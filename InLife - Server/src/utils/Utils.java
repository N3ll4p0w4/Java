package utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author pigro
 */
public class Utils {

    public static ArrayList<String> getStringsFromStringWithPuntoEVirgola(String s){
        ArrayList<String> stringhe = new ArrayList<>();
        while(s.contains(";")){
            String s1 = s.substring(0, s.indexOf(";"));
            stringhe.add(s1);
            s = s.substring(s.indexOf(";")+1);
        }
        return stringhe;
    }
    
    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height, int quality){
		
	BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
	Graphics2D g = resizedImage.createGraphics();
	g.drawImage(originalImage, 0, 0, width, height, null);
	g.dispose();
	
	return resizedImage;
    }
}
