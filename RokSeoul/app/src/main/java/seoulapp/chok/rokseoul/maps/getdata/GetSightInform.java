package seoulapp.chok.rokseoul.maps.getdata;


import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by JIEUN on 2016-09-27.
 */

public class GetSightInform extends AsyncTask<String, Void, Document> {

    Document doc = null;

    public String allData;

    String returnData;

    @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
                doc.getDocumentElement().normalize();

/*
                //----------
                allData = "";
                //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
                NodeList nodeList = doc.getElementsByTagName("item");
                //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환

                Node nameNode = nodeList.item(0);
                Element nameElmnt = (Element) nameNode;
                String sightsID = nameElmnt.getElementsByTagName("contentid").item(0).getChildNodes().item(0).getNodeValue();
                if(sightsID.equals("126508")){
                    allData+= "관광지명 : 경복궁 \n";
                }else if(sightsID.equals("126535")){
                    allData+= "관광지명 : N서울타워(남산타워) \n";
                }
                allData+= "이용시간 : "+
                        nameElmnt.getElementsByTagName("usetime").item(0).getChildNodes().item(0).getNodeValue()+"\n";
                allData+= "쉬는날 : "+
                        nameElmnt.getElementsByTagName("restdate").item(0).getChildNodes().item(0).getNodeValue()+"\n";
                allData+= "주차시설 : "+
                        nameElmnt.getElementsByTagName("parking").item(0).getChildNodes().item(0).getNodeValue()+"\n";
                allData.replaceAll("<"+"br"+" />", "\n");
*/
            } catch (Exception e) {
                Log.d("soonsu","3");
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document document) {
/*            allData = "";
            //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("item");
            //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환

            Node nameNode = nodeList.item(0);
            Element nameElmnt = (Element) nameNode;
            String sightsID = nameElmnt.getElementsByTagName("contentid").item(0).getChildNodes().item(0).getNodeValue();
            if(sightsID.equals("126508")){
                allData+= "관광지명 : 경복궁 \n";
            }else if(sightsID.equals("126535")){
                allData+= "관광지명 : N서울타워(남산타워) \n";
            }
            allData+= "이용시간 : "+
                    nameElmnt.getElementsByTagName("usetime").item(0).getChildNodes().item(0).getNodeValue()+"\n";
            allData+= "쉬는날 : "+
                    nameElmnt.getElementsByTagName("restdate").item(0).getChildNodes().item(0).getNodeValue()+"\n";
            allData+= "주차시설 : "+
                    nameElmnt.getElementsByTagName("parking").item(0).getChildNodes().item(0).getNodeValue()+"\n";
            allData.replaceAll("<"+"br"+" />", "\n");

            returnData = allData;

            super.onPostExecute(document);*/
        }

}
