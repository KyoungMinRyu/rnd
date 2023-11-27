package com.lam.rnd.scheduler;

import com.lam.rnd.util.JsonParsing;
import com.lam.rnd.util.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@Controller
public class Scheduler
{
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    private final LocalDateTime localDateTime = LocalDateTime.now();
    private static StringBuilder stringBuilder;

    private static StringBuilder tempBuilder;

    private final String path = "src/main/resources/rnd/";

    private List<String> urlList = null;
    public Scheduler()
    {
        stringBuilder = new StringBuilder();
        tempBuilder = new StringBuilder();
        urlList = JsonParsing.parsingArr(JsonParsing.readFile("src/main/resources/url.json"));
    }

    public static StringBuilder getStringBuilder()
    {
        return stringBuilder;
    }


    private static void setStringBuilder(StringBuilder stringBuilder)
    {
        Scheduler.stringBuilder = stringBuilder;
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    private void deleteFiles()
    {
        String date = localDateTime.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyyMM"));
        File[] files = new File(path.substring(0, path.length() - 1)).listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isFile() && file.getName().substring(0, 6).equals(date))
                {
                    file.delete();
                }
            }
        }
    }

    @Scheduled(cron = "0 30 13 * * ?")
    private void rndCrawling()
    {
        System.out.println("===========================================================================================");
        System.out.println("R&D Crawling Start");

        StringBuilder rndBuilder;
        String url = "";
        Document doc = null;
        Elements elements = null;
        for(int i = 0; i < urlList.size(); i++)
        {
            url = JsonParsing.parsingJson(urlList.get(i), "url");
            if(i  == 0)
            {
                int lastIndex = 0;
                int nextPaingIndex = 0;
                String lastStr = "";
                doc = crawlingPage(url);

                elements = doc.select(".group1");
                lastStr = doc.select(".paginate").select(".page_last").toString();
                nextPaingIndex = doc.select(".paginate").select(".page_none").size();

                if(!StringUtil.isEmpty(lastStr)) // 페이지 내에 맨 끝으로 가는 페이징 처리가 있을 경우
                {
                    lastIndex = Integer.parseInt(StringUtil.subString(lastStr, "(", ")"));
                }

                getRnDInfoList(tempBuilder, elements, url);

                if(lastIndex > 0) // 맨 끝까지 크롤링  pageIndex
                {
                    for(int j = 2; j <= lastIndex; j++)
                    {
                        doc = crawlingPage(url + "&pageIndex=" + j);
                        getRnDInfoList(tempBuilder, doc.select(".group1"), url +"&pageIndex=" + j);
                    }
                }
                else if(nextPaingIndex > 0) // 끝으로 가는 버튼은 없고 페이징 처리가 존재할 경우
                {
                    for(int j = 0; j < nextPaingIndex; j++)
                    {
                        doc = crawlingPage(url + "&pageIndex=" + (j + 1));
                        getRnDInfoList(tempBuilder, doc.select(".group1"), url +"&pageIndex=" + j);
                    }
                }
            }
            else if (i == 1)
            {
                doc = crawlingPage(url);
                String str = doc.toString();

                rndBuilder = new StringBuilder(str.substring(str.lastIndexOf("<script>"), str.lastIndexOf("</script>")));

                rndBuilder = new StringBuilder(rndBuilder.substring(rndBuilder.indexOf("var newHtml") + 10));

                List<String> titleList = new ArrayList<String>();

                List<String> regDateList = new ArrayList<String>();

                String temp = "";

                int index = 0;

                while(true)
                {
                    index = rndBuilder.indexOf("var newHtml");
                    if(index != -1)
                    {
                        temp = rndBuilder.substring(0, index);
                        titleList.add(temp.substring(temp.indexOf("sHtml+= unescape('") + 18, temp.indexOf("');")));
                        rndBuilder.delete(0, index + 10);
                    }
                    else
                    {
                        titleList.add(rndBuilder.substring(rndBuilder.indexOf("sHtml+= unescape('") + 18, rndBuilder.indexOf("');")));
                        break;
                    }
                }

                rndBuilder.delete(0, rndBuilder.indexOf("$('#td_'+'REG_DT'+'_0').html('"));

                rndBuilder =  new StringBuilder(rndBuilder.toString().replaceAll("\r|\n|\t", ""));


                while(true)
                {
                    index = rndBuilder.indexOf("var replyHrml");
                    if(index != -1)
                    {
                        temp = rndBuilder.substring(index - 13, index - 3);
                        regDateList.add(temp);
                        rndBuilder.delete(0, index + 10);
                    }
                    else
                    {
                        regDateList.add(rndBuilder.substring(rndBuilder.lastIndexOf(";") - 12 , rndBuilder.lastIndexOf(";") - 2));
                        break;
                    }
                }

                for(int j = 0; j < titleList.size(); j++)
                {
                    getRnDInfoList(tempBuilder, titleList.get(j), regDateList.get(j), url);
                }
            }
            else if (i == 2)
            {
                for(int j = 0; j < 5; j++)
                {
                    doc = crawlingPage(url + (j * 10 + 1));

                    elements = doc.select("tbody").select("tr");

                    for(int z = 0; z < elements.size(); z++)
                    {
                        Elements element = elements.get(z).select("td");
                        getRnDInfoList(tempBuilder, element.select("a").text(), element.get(3).text(), url + (j * 10 + 1));
                    }
                }
            }
            else if (i == 3)
            {
                doc = crawlingPage(url);

                elements = doc.select("tbody").select("tr");

                for(int z = 0; z < elements.size(); z++)
                {
                    Element element = elements.get(z);
                    getRnDInfoList(tempBuilder, element.select("a").text(), element.select("td").get(2).text(), url);
                }
            }
            else if (i == 4)
            {
                doc = crawlingPage(url);

                elements = doc.select("tbody").select("tr");
                for(int z = 0; z < elements.size(); z++)
                {
                    Element element = elements.get(z);
                    getRnDInfoList(tempBuilder, element.select(".td_title").text(), element.select(".td_reg_date").text(), url);
                }
            }
            else if (i == 5)
            {
                doc = crawlingPage(url);

                elements = doc.select(".board_type01").select("li");

                for(int z = 0; z < elements.size(); z++)
                {
                    Element element = elements.get(z);
                    String str = element.select(".subject").toString();
                    getRnDInfoList(tempBuilder, StringUtil.subString(str, "[", "<em>"), element.select(".src").select("em").get(0).text(), url);
                }
            }
            else if (i == 6)
            {
                doc = crawlingPage(url);

                elements = doc.select("tbody").select("tr");

                for(int z = 0; z < elements.size(); z++)
                {
                    Elements element = elements.get(z).select("td");
                    getRnDInfoList(tempBuilder, element.get(1).text(), element.get(6).text(), url);
                }
            }
            else if (i == 7)
            {
                doc = crawlingPage(url);

                elements = doc.select("tbody").select("tr");

                for(int z = 0; z < elements.size(); z++)
                {
                    Elements element = elements.get(i).select("td");
                    getRnDInfoList(tempBuilder, element.get(2).text(), element.get(4).text(), url);
                }
            }
            else
            {
                break;
            }
        }

        JsonParsing.writeFile(path, getNowDate() + ".txt", tempBuilder.toString());

        setStringBuilder(tempBuilder);

        System.out.println("R&D Crawling End");
        System.out.println("===========================================================================================");
    }

    private String getElementSelecter(Element element, String cssClass)
    {
        return element.select(cssClass).text();
    }


    private Document crawlingPage(String url)
    {
        try
        {
            return Jsoup.connect(url).post();
        }
        catch (IOException e)
        {
            logger.error(e.toString());
        }
        return null;
    }

    public void getRnDInfoList(StringBuilder stringBuilder, Elements elements, String url)
    {
        for (Element element : elements)
        {
            stringBuilder
                    .append(getElementSelecter(element, ".title")).append(", ")
                    .append(StringUtil.subString(getElementSelecter(element, ".ancmDe"), ":")).append(", ")
                    .append(url).append("\n\r");
        }
    }

    public void getRnDInfoList(StringBuilder stringBuilder, String title, String regDate, String url)
    {
        stringBuilder
                .append(title).append(", ")
                .append(regDate).append(", ")
                .append(url).append("\n\r");

    }
    public String getNowDate()
    {
        return  localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
