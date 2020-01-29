import java.io.*;
import java.util.ArrayList;

public class configuracion{

    File archivo = null;
    FileReader fr = null;
    BufferedReader br = null;
    FileWriter fw = null;
    BufferedWriter bw = null;

    public String get(String campo) {
        
        try {
            archivo = new File ("configuracion.txt");
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);

            // Lectura del fichero
            String linea;
            while((linea=br.readLine())!=null){
                if(linea.substring(0,1).equals("#"));
                else if(linea.substring(0,linea.indexOf("=")).equals(campo)) {
                    br.close(); fr.close();
                    return (linea.substring(linea.indexOf("=")+1));
                }
            }
            br.close(); fr.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        return "Cant find the number os players";
    }

    public void set(String campo,String valor){
        
        ArrayList<String> aux = new ArrayList<String>();

        try {
            archivo = new File ("configuracion.txt");
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);
            int i = 0;
            String linea;
            while((linea=br.readLine())!=null){
                
                if(linea.substring(0,1).equals("#"))aux.add(linea);//we need that not to try to find "=" in a comment
                else if(linea.substring(0,linea.indexOf("=")).equals(campo)) {
                    aux.add(linea.substring(0,linea.indexOf("=")+1) + valor);
                }else{
                    aux.add(linea);
                }
                i += 1;
            }
            br.close(); fr.close();
        //Ahora en aux temos todo o que queremos escribir, asique reescribirmos o arquivo completo
            fw = new FileWriter(archivo);
            bw = new BufferedWriter(fw);
            i=0;
            while(i<aux.size()){
                System.out.println(aux.get(i));
                bw.write(aux.get(i)+"\n");
                i+=1;
            }
        bw.close();fw.close();
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}