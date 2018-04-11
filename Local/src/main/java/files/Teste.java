package files;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Teste {



    public static void main(String[] args) throws Exception {
        Path path = Paths.get("/home/padrao/Desktop/4ano/Distributed Systems Principles and Paradigms.pdf");
        byte[] data = Files.readAllBytes(path);

        System.out.println(data.length);

        HashMap<Integer,byte[]> blocos = new HashMap<>();

        byte[] insert = new byte[100000];
        byte[] novo;

        //blocos de 100 k

        int i, j, k;

//        if(data.length > 100000){
//
//            for(i = 0, j = 0, k = 0; i<data.length; i++, j++){
//
//                insert[j] = data[i];
//
//                if(j == 99999){
//                    blocos.put(k,insert);
//                    insert = new byte[100000];
//                    k++;
//                    j = -1;
//                }
//            }
//
//            if(j != 0){
//
//                byte[] trim = new byte[j];
//
//                for(i=0; i<j; i++){
//
//                    trim[i]=insert[i];
//                }
//
//                blocos.put(k,trim);
//            }
//
//            i = 0;
//            novo = new byte[data.length];
//
//            for (Map.Entry<Integer, byte[]> entry : blocos.entrySet())
//            {
//                for(byte b : entry.getValue()){
//
//                    novo[i] = b;
//                    i++;
//                }
//            }
//        }
//        else { // se tiver menos de 100 k for so num bloco
//
//            novo = data;
//        }

        //System.out.println(novo.length);

        //Files.write(new File("/home/padrao/Desktop/4ano/lol.pdf").toPath(), novo);


    }


}
