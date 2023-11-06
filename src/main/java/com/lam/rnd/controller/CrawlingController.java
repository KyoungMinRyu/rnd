package com.lam.rnd.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


@Controller("crawlingController")
public class CrawlingController
{
    @RequestMapping(value = "/test")
    public String test()
    {
        String URL = "https://www.iris.go.kr/contents/retrieveBsnsAncmBtinSituListView.do";
        String str = "";
        try
        {
            Document doc = Jsoup.connect(URL).get();
            str = doc.select(".group1").toString();
            System.out.println(doc.select(".group1"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return str;
    }

}
