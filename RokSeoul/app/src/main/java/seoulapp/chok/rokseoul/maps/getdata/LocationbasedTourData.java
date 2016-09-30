package seoulapp.chok.rokseoul.maps.getdata;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import seoulapp.chok.rokseoul.R;

/**
 * Created by JIEUN on 2016-09-23.
 */

public class LocationbasedTourData extends AppCompatActivity {

    Document doc = null;
    private TextView tv;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.json_test);
        tv = (TextView) findViewById(R.id.tvReslut1);
        Button b = (Button)findViewById(R.id.button);
        Log.d("soonsu","0");
        b.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.d("soonsu","1");
                GetXMLTask task = new GetXMLTask();
                //task.execute("http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailCommon?ServiceKey=pAfNXzuf%2B9ZoTgQ9ckBQOlCUzLNozWj6Am72wZ%2B57zK3c%2FjStotNWWUd2Na4PPsq1Ugcq18kbUWE%2F6QfkuloIQ%3D%3D&contentId=636266&contentTypeId=12&defaultYN=Y&mapImageYN=Y&firstImageYN=Y&areacodeYN=N&catcodeYN=N&addrinfoYN=N&mapinfoYN=N&overviewYN=N&transGuideYN=N&MobileOS=ETC&MobileApp=%EA%B3%B5%EC%9C%A0%EC%9E%90%EC%9B%90%ED%8F%AC%ED%84%B8&numOfRows=999&pageNo=1");

                try {
                    StringBuilder urlBuilder = new StringBuilder("http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailIntro"); /*URL*/

                    /*필요 파라미터*/
                    urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=pAfNXzuf%2B9ZoTgQ9ckBQOlCUzLNozWj6Am72wZ%2B57zK3c%2FjStotNWWUd2Na4PPsq1Ugcq18kbUWE%2F6QfkuloIQ%3D%3D"); /*Service Key*/
                    urlBuilder.append("&" + URLEncoder.encode("contentTypeId", "UTF-8") + "=" + URLEncoder.encode("12", "UTF-8")); /*타입 ID*/
                    urlBuilder.append("&" + URLEncoder.encode("contentId", "UTF-8") + "=" + URLEncoder.encode("126508", "UTF-8")); /*콘텐츠ID  경복궁 : 126508, N서울타워 :126535*/
                    urlBuilder.append("&" + URLEncoder.encode("MobileOS", "UTF-8") + "=" + URLEncoder.encode("ETC", "UTF-8")); /*OS 구분*/
                    urlBuilder.append("&" + URLEncoder.encode("MobileApp", "UTF-8") + "=" + URLEncoder.encode("RokSEOUL", "UTF-8")); /*어플이름*/
                    urlBuilder.append("&" + URLEncoder.encode("introYN", "UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8")); /*뭔지 모르겠는데 인트로*/

                    task.execute(urlBuilder.toString());
                }catch (Exception e){

                }
            }
        });
    }

    private class GetXMLTask extends AsyncTask<String, Void, Document>{

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
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document document) {

            String s = "";
            //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("item");
            //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환

            Node nameNode = nodeList.item(0);
            Element nameElmnt = (Element) nameNode;
            String sightsID = nameElmnt.getElementsByTagName("contentid").item(0).getChildNodes().item(0).getNodeValue();
            if(sightsID.equals("126508")){
                s+= "관광지명 : 경복궁 \n";
            }else if(sightsID.equals("126535")){
                s+= "관광지명 : N서울타워(남산타워) \n";
            }
            s+= "이용시간 : "+
                    nameElmnt.getElementsByTagName("usetime").item(0).getChildNodes().item(0).getNodeValue()+"\n";
            s+= "쉬는날 : "+
                    nameElmnt.getElementsByTagName("restdate").item(0).getChildNodes().item(0).getNodeValue()+"\n";
            s+= "주차시설 : "+
                    nameElmnt.getElementsByTagName("parking").item(0).getChildNodes().item(0).getNodeValue()+"\n";
            String s2 = s.replaceAll("<"+"br"+" />", "");

            System.out.println("allData : "+s);
            System.out.println("allData2 : "+s2);

            tv.setText(s2);
            super.onPostExecute(document);
        }
    }
}
