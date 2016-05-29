import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class SeamCarving {
	private int imageWidth;
	private int imageHeight;




public static void main(String[] args){
	if (args.length < 5){
		System.out.println("Not enough arguments provided.");
		return;
	}
	SeamCarving seam_carving = new SeamCarving();

	
	String input_filename = args[0];
	int output_columns = Integer.parseInt(args[1]),
		output_rows = Integer.parseInt(args[2]),
		energy_type = Integer.parseInt(args[3]);
	String output_filename = args[4];

	BufferedImage img = null;
	

		
	try 
	{
		img = ImageIO.read(new File(input_filename));
	} 
	catch (IOException e) 
	{
	    e.printStackTrace();
	}
	
	seam_carving.imageHeight = img.getHeight();
	seam_carving.imageWidth = img.getWidth();
	System.out.printf("heigth= %d \n", seam_carving.imageHeight);
	System.out.printf("width= %d \n", seam_carving.imageWidth);
	
	int num_of_horizontal_seam = Math.abs(seam_carving.imageWidth - output_columns),
			num_of_vertical_seam =  Math.abs(seam_carving.imageHeight - output_rows);
	int decreasing_or_increasing_width = (seam_carving.imageWidth > output_columns)? 0:1; //decreasing is 0 increasing is 1.
	int decreasing_or_increasing_heigth = (seam_carving.imageHeight > output_rows)? 0:1; //decreasing is 0 increasing is 1.

	double [][] Energy; double [][] dynamic_map; 
	Image image;
	int horizontal=0, vertical=0;
	boolean transpose=false;
	//int width = seam_carving.imageWidth; int height = seam_carving.imageHeight;
	while ((num_of_horizontal_seam) > 0 || (num_of_vertical_seam) > 0){
		if (num_of_horizontal_seam > 0){//Decide on a seam direction – horizontal.
			
			horizontal=1;
		}
		else if (num_of_vertical_seam > 0){//Decide on a seam direction – vertical.
			if (!transpose){
				img = seam_carving.transposeBufferImage(img);
				transpose=true;
				}
			seam_carving.setImageHeight(img.getHeight());
			seam_carving.setImageWidth(img.getWidth());
			System.out.printf("vertical heigth= %d \n", seam_carving.imageHeight);
			System.out.printf("vertical width= %d \n", seam_carving.imageWidth);
			vertical=1;

			
		}
		
		Energy = seam_carving.getEnergy(img,energy_type);
		dynamic_map = seam_carving.dynamic_programming_map(Energy,seam_carving.getImageWidth(), seam_carving.getImageHeight());
		int[][] lowestEnergySeam = seam_carving.findingLowestEnergySeam(dynamic_map, seam_carving.getImageWidth(),seam_carving.getImageHeight());
		img = seam_carving.removeSeam(img, lowestEnergySeam);
		seam_carving.setImageWidth(seam_carving.getImageWidth()-1);
		File outputfile = new File(output_filename);
		try{
		ImageIO.write(img, "png", outputfile); // Write the Buffered Image into an output file
		image  = ImageIO.read(new File(output_filename));
		}
		catch (IOException e) 
		{
		    e.printStackTrace();
		}
		
		if (horizontal==1) 			num_of_horizontal_seam--;
		else {
			
			//img = seam_carving.transposeBufferImage(seam_carving.transposeBufferImage(seam_carving.transposeBufferImage(img)));
			num_of_vertical_seam--;


		}
		horizontal=0;;
		
	}
	if (vertical==1){
		if (transpose)
			img = seam_carving.notTransposeBufferImage(img);
		File outputfile = new File(output_filename);
		try{
		ImageIO.write(img, "png", outputfile); // Write the Buffered Image into an output file
		image  = ImageIO.read(new File(output_filename));
		}
		catch (IOException e) 
		{
		    e.printStackTrace();
		}
	}

	System.out.println("ok");

}


public int getImageWidth() {
	return imageWidth;
}


public void setImageWidth(int imageWidth) {
	this.imageWidth = imageWidth;
}


public int getImageHeight() {
	return imageHeight;
}


public void setImageHeight(int imageHeight) {
	this.imageHeight = imageHeight;
}


double [][] getEnergy(BufferedImage img, int energy_type){
	int w = img.getWidth();
	int h = img.getHeight();
	double [][] redEnergy = new double[w][h]; 
	double [][] greenEnergy = new double[w][h];
	double [][] blueEnergy = new double[w][h]; 
	double [][] resEnergy = new double[w][h]; 


	for (int j=0;j<h;j++){
		for (int i=0;i<w;i++){
			Color color = new Color(img.getRGB(i,j));
			redEnergy[i][j]= color.getRed();
			greenEnergy[i][j] = color.getGreen();
			blueEnergy[i][j] = color.getBlue();

		}
	}
	System.out.printf("w=%d, h=%d \n", w,h);

	for (int j=0;j<h;j++){
		for (int i=0;i<w;i++){
			resEnergy[i][j] =  eneryValueOfPixel(redEnergy,greenEnergy,blueEnergy,i,j);
			if (energy_type==1){
				resEnergy[i][j] = entropy(redEnergy,greenEnergy,blueEnergy, i, j)/2 + resEnergy[i][j]/2;
				
			}
		}
	}
	
	
	
	
	
	return resEnergy;
	
}
double eneryValueOfPixel(double [][] red, double [][] green, double [][] blue, int i, int j){
	int num_of_neihbors = 0;
	double val = 0, r = red[i][j], g = green[i][j], b= blue[i][j];
	if (i > 0){
		val += energyValueOneNeigbor(red[i-1][j], green[i-1][j], blue[i-1][j], r, g, b);
		num_of_neihbors++;
	}
	if (j > 0){
		val += energyValueOneNeigbor(red[i][j-1], green[i][j-1], blue[i][j-1], r, g, b);
		num_of_neihbors++;
	}
	if ((i > 0)&&(j > 0))
	{
		val += energyValueOneNeigbor(red[i-1][j-1], green[i-1][j-1], blue[i-1][j-1], r, g, b);
		num_of_neihbors++;
	}
	if (i < this.imageWidth-1)
	{
		val += energyValueOneNeigbor(red[i+1][j], green[i+1][j], blue[i+1][j], r, g, b);
		num_of_neihbors++;
	}
	
	if (j < this.imageHeight-1)
	{
		val += energyValueOneNeigbor(red[i][j+1], green[i][j+1], blue[i][j+1], r, g, b);
		num_of_neihbors++;
	}
	if ((i < this.imageWidth-1) && (j < this.imageHeight-1))
	{
		val += energyValueOneNeigbor(red[i+1][j+1], green[i+1][j+1], blue[i+1][j+1], r, g, b);
		num_of_neihbors++;
	}
	if ((i < this.imageWidth-1) && (j > 0)){
		val += energyValueOneNeigbor(red[i+1][j-1], green[i+1][j-1], blue[i+1][j-1], r, g, b);
		num_of_neihbors++;
	}
	if ((j < this.imageHeight-1) && (i > 0)){
		val += energyValueOneNeigbor(red[i-1][j+1], green[i-1][j+1], blue[i-1][j+1], r, g, b);
		num_of_neihbors++;
	}
	
	return val/num_of_neihbors;
	
}

double energyValueOneNeigbor (double R, double G, double B, double currR, double currG, double currB){
	double res = (Math.abs(currR-R) + Math.abs(currG-G) + Math.abs(currB-B))/3;
	return res;
	
}

double entropy(double [][] red, double [][] green, double [][] blue, int i, int j){
	double sum = 0;
	int cnt=0;
	double greyScale;
	for (int n= i-4; n<i+4;n++ ){
		for (int m= j-4; m<j+4;m++ ){
			if ((n >=0) && (m>=0) && (n<imageWidth) && (m<imageHeight)){
			greyScale = greyScalePm(red, green, blue, n, m);
			sum += greyScale*Math.log(greyScale);
			cnt++;
		
			}
			}
	}
	
	return -sum/cnt;
	
}

double greyScalePm(double [][] red, double [][] green, double [][] blue, int i, int j)
{
	double res = (red[i][j] + green[i][j]+blue[i][j])/3;
	double sum = 0;
	for (int k= i-4; k<i+4;k++ ){
		for (int l= j-4; l<j+4;l++ ){
			if ( (k >=0) && (l >=0) && (k < imageWidth) && (l < imageHeight)){
				sum += (red[k][l] + green[k][l]+blue[k][l])/3;
			}
		}
	}
	
	
	return res/sum;

}


double[][] dynamic_programming_map(double [][] energy, int w,int h){
	double [][] res = new double[w][h];
	for(int j=0;j<h;j++){
		for (int i=0;i<w;i++){
			if (i==0){
				res[i][j] = energy[i][j];
			}
				
			else if (j == h-1 ){
				res[i][j] = energy[i][j] + Math.min(energy[i-1][j-1],energy[i-1][j]);
			}
			else if (j == 0){
				res[i][j] = energy[i][j] + Math.min(energy[i-1][j],energy[i-1][j+1]);

			}
			else{
				res[i][j] = energy[i][j] + Math.min(Math.min(energy[i-1][j-1],energy[i-1][j]),energy[i-1][j+1]);
			}
	
		}
		}
	
	
	return res;
	
	
}

int[][] findingLowestEnergySeam(double[][] dynamic_map, int w, int h){
	int[][] result = new int[w][h];
	double min = dynamic_map[0][h-1];
	Pixel pixel = new Pixel();
	for (int i=0;i<w;i++){
		if (min > dynamic_map[i][h-1]){
			min = dynamic_map[i][h-1];
			pixel.setPixel(i, h-1);
		}
	}
	result[pixel.getI()][h-1]=1;

	Pixel first,second,third;
	int i = pixel.getI();
	for(int j=h-1;j>0;j--){

		first=null;
		second=null;
		third = null;

		first = new Pixel(i,j-1);
		if (i < w-1){
			second = new Pixel(i+1,j-1);
		}
		
		if (i > 0){
			if (second==null)
				second = new Pixel(i-1,j-1);
			else
				third = new Pixel(i-1,j-1);
		}
		pixel = chooseLowestEnergyNeihbor(first, second,third,dynamic_map);
		i = pixel.getI();
		result[pixel.getI()][pixel.getJ()]=1;
		//System.out.printf("chosen pixel= %d %d \n",pixel.getI(),pixel.getJ());

	
		
	}
	System.out.printf("w= %d h=%d \n",w,h);

	for (int p=0;p<h;p++){
		for (int k=0;k<w;k++){

			if (result[k][p]==1){
				System.out.printf("good=%d	,%d\n", k,p);
				
			}
			
			
		}
	}
	System.out.println("\n");
	



	
	return result;

}

Pixel chooseLowestEnergyNeihbor(Pixel first, Pixel second, Pixel third,double[][] dynamic_map){
	double min;
//	System.out.printf("pixel1: x= %d, y=%d\n", first.getI(),first.getJ());
//	if (second!=null)
//		System.out.printf("pixel2: x= %d, y=%d\n", second.getI(),second.getJ());
//	else System.out.println("second=null");
	double tmp1,tmp2=-100,tmp3=-100;
	tmp1 = dynamic_map[first.getI()][first.getJ()];
	if (second!=null)
		tmp2 = dynamic_map[second.getI()][second.getJ()];
	else if(third!=null)
		tmp3 = dynamic_map[third.getI()][third.getJ()];
	//System.out.printf("tmp1= %f, tmp2=%f, tmp3=%f	",tmp1,tmp2,tmp3);
	if (third!=null){
		min = Math.min(Math.min(dynamic_map[first.getI()][first.getJ()], dynamic_map[second.getI()][second.getJ()]), dynamic_map[third.getI()][third.getJ()]);
		//System.out.printf("min = %f, first=%f, second=%f\n", min,dynamic_map[first.getI()][first.getJ()],dynamic_map[second.getI()][second.getJ()]);
	}
	else if (second!=null){
		min = Math.min(dynamic_map[first.getI()][first.getJ()], dynamic_map[second.getI()][second.getJ()]);

	}
	
	else
		return first;
	if (min==dynamic_map[first.getI()][first.getJ()]) return first;
	else if (min==dynamic_map[second.getI()][second.getJ()]) return second;
	else return third;

}

BufferedImage removeSeam (BufferedImage img, int[][] seam){
	BufferedImage newImg = new BufferedImage(img.getWidth()-1,img.getHeight(), BufferedImage.TYPE_INT_ARGB);
	int color;
	System.out.printf("newImg w = %d, h=%d\n", newImg.getWidth(), newImg.getHeight());
	for (int j=0; j<img.getHeight(); j++){
		boolean shift = false;
		for (int i=0; i<img.getWidth();i++){
			if(seam[i][j]==1) {
				shift = true;
				if(j==967)
					System.out.printf("okk = %d, %d\n", i,j);

				//System.out.printf("okk = %d, %d\n", i,j);
				continue;
			}
			//System.out.printf("hello = %d, %d\n", i,j);

			if (shift){
				color = img.getRGB(i,j);
				newImg.setRGB(i-1, j, color);
			}
			else{
				color = img.getRGB(i,j);
				newImg.setRGB(i, j, color);
			}
			

			
		}
		
		
	}
	
	
	
	
	return newImg;
	
}

BufferedImage transposeBufferImage(BufferedImage img){
	int color;
	BufferedImage newImg = new BufferedImage(img.getHeight(),img.getWidth(), BufferedImage.TYPE_INT_ARGB);
	for (int i=0; i<img.getWidth(); i++){
		for (int j=0; j<img.getHeight();j++){
			color = img.getRGB(i,j);
			newImg.setRGB(j, i, color);
		}
		}
	
	
	
	return newImg;

}


BufferedImage notTransposeBufferImage(BufferedImage img){
	int color;
	BufferedImage newImg = new BufferedImage(img.getHeight(),img.getWidth(), BufferedImage.TYPE_INT_ARGB);
	newImg = this.transposeBufferImage(img);
//	for (int j=0; j<img.getWidth(); j++){
//		for (int i=0; i<img.getHeight();i++){
//			color = img.getRGB(i,j);
//			newImg.setRGB(j, i, color);
//		}
//		}
	
	return newImg;

}






}