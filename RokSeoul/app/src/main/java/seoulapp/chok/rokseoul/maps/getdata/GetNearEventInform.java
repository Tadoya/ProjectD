package seoulapp.chok.rokseoul.maps.getdata;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by JIEUN on 2016-09-28.
 */

public class GetNearEventInform {


    Document doc = null;

    String SightName;
    String usetime;
    String restdate;
    String parking;
    String allData;

    public void getXMLData(){
        GetXMLTask task = new GetXMLTask();
        try {
            StringBuilder urlBuilder = new StringBuilder("http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList"); /*URL*/

            /*필요 파라미터*/
            urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=pAfNXzuf%2B9ZoTgQ9ckBQOlCUzLNozWj6Am72wZ%2B57zK3c%2FjStotNWWUd2Na4PPsq1Ugcq18kbUWE%2F6QfkuloIQ%3D%3D"); /*Service Key*/
            urlBuilder.append("&" + URLEncoder.encode("contentTypeId", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8")); /*타입 ID*/
            urlBuilder.append("&" + URLEncoder.encode("mapX", "UTF-8") + "=" + URLEncoder.encode("126.981106", "UTF-8")); /*X좌표*/
            urlBuilder.append("&" + URLEncoder.encode("mapY", "UTF-8") + "=" + URLEncoder.encode("37.568477", "UTF-8")); /*Y좌표*/
            urlBuilder.append("&" + URLEncoder.encode("radius", "UTF-8") + "=" + URLEncoder.encode("2000", "UTF-8")); /*반경범위*/
            urlBuilder.append("&" + URLEncoder.encode("listYN", "UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8")); /*리스팅 ID*/
            urlBuilder.append("&" + URLEncoder.encode("MobileOS", "UTF-8") + "=" + URLEncoder.encode("ETC", "UTF-8")); /*OS 구분*/
            urlBuilder.append("&" + URLEncoder.encode("MobileApp", "UTF-8") + "=" + URLEncoder.encode("RokSEOUL", "UTF-8")); /*어플이름*/
            urlBuilder.append("&" + URLEncoder.encode("arrange", "UTF-8") + "=" + URLEncoder.encode("A", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("12", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));

            task.execute(urlBuilder.toString());
        }catch (Exception e){

        }
    }

    private class GetXMLTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                Log.d("soonsu","2");
                url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
                doc.getDocumentElement().normalize();

            } catch (Exception e) {
                Log.d("soonsu","3");
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document document) {
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
            System.out.println("allData : "+allData);
            //s.replaceAll("<"+"br"+" />", "\n");


            super.onPostExecute(document);
        }
    }

}
