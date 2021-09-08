package cp_heuristics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class Test_Batch {
	
	public static void main(String[] args) {
		Batch_Placement placer = new Batch_Placement();
		double w = 15;
		double h = 86.4;
		double d = 140;
		double[] bin_dimensions = new double[3];
		bin_dimensions[0] = 235;
		bin_dimensions[1] = 268.7;
		bin_dimensions[2] = 1203.2;
		Box box_template = new Box(w, h, d);
		
		//Box.Orientation[] allowed_ori = {Box.Orientation.Upright, Box.Orientation.Side, Box.Orientation.SideRotate, Box.Orientation.Lie};
		//Box.Orientation[] allowed_ori = {Box.Orientation.Upright, Box.Orientation.UprightRotate, Box.Orientation.Side, Box.Orientation.SideRotate};
		//box_template.set_allowed_ori(allowed_ori);
		
		long time0 = System.currentTimeMillis();
		//Vector<Box> boxes = placer.test_fill_layer(box_template, bin_dimensions);
		//Vector<Box> boxes = placer.fill_bin_single_pass(box_template, bin_dimensions);
		Vector<Box> boxes = placer.fill_bin_exhaustive_search(box_template, bin_dimensions);
		long delta_time = System.currentTimeMillis()-time0;
		
		// check validity of boxes placement
		Utility_functions uf = new Utility_functions();
		boolean overlap = uf.box_overlap_check(boxes);
		if (overlap == false) {
			System.out.println("overlap check passed");
		}
		
		// print some results
		System.out.printf("%d boxes are packed \n", boxes.size());
		double bin_volume = bin_dimensions[0]*bin_dimensions[1]*bin_dimensions[2];
		double[] box_dimensions = boxes.elementAt(0).get_dimensions();
		double box_volume = box_dimensions[0]*box_dimensions[1]*box_dimensions[2];
		double fill_percentage_3d = boxes.size()*box_volume/bin_volume;
		System.out.printf("%f space occupied \n", fill_percentage_3d);
		System.out.printf("time elapsed: %d milliseconds \n", delta_time);
		// save results
		save_results(boxes, bin_dimensions);
		System.out.println("finished");
	}
	
	private static void save_results(Vector<Box> boxes, double[] bin_dimensions) {
		// save packing result
		String filename = "result.csv";
		String s;
		Box box;
		double[] position, dimensions;
		File file = new File(filename);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (int j=0; j<boxes.size(); j++) {
				box = boxes.elementAt(j);
				position = box.get_position();
				dimensions = box.get_dimensions();
				s = Integer.toString(0)+", " 
				    +Double.toString(position[0])+", " +Double.toString(position[1])+", " +Double.toString(position[2])+", "
				    +Double.toString(dimensions[0])+", " +Double.toString(dimensions[1])+", " +Double.toString(dimensions[2]);
				writer.write(s);
				writer.newLine();
			}
			writer.close();
		} catch (FileNotFoundException ex) {
			System.out.println("file not exist");
		} catch (IOException ex) {
			System.out.println("error writing file");
		}
		// save bin dimensions
		filename = "bin_dimensions.csv";
		file = new File(filename);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			s = Double.toString(bin_dimensions[0])+", " +Double.toString(bin_dimensions[1])+", " +Double.toString(bin_dimensions[2]);
			writer.write(s);
			writer.close();
		} catch (FileNotFoundException ex) {
			System.out.println("file not exist");
		} catch (IOException ex) {
			System.out.println("error writing file");
		}	
	}

}
