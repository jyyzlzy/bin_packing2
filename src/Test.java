package cp_heuristics;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.BufferedWriter;
import java.io.*;

public class Test {
	
	public static void main(String[] args) {
		double[] bin_dimensions = define_bin_dimensions();
		
		
		// solve the sequence placement task
		// define task
		Vector<Box> box_sequence = generate_box_sequence();
		// solve sequence task
		Vector<Bin> bins = new Vector<Bin>();
		long time0 = System.currentTimeMillis();
		Vector<Place_info> how_to_place = solve_box_sequence(bin_dimensions, box_sequence, bins);
		long delta_time = System.currentTimeMillis()-time0;
		// show placement of each box
		show_boxes_placement(box_sequence, how_to_place);
		System.out.printf("time elapsed: %d milliseconds \n", delta_time);
		
		
		// solve single type box placement task
		/*
		Box box_template = get_single_box();
		long time0 = System.currentTimeMillis();
		Bin bin = solve_single_type_box(bin_dimensions, box_template);
		long delta_time = System.currentTimeMillis()-time0;
		Vector<Bin> bins = new Vector<Bin>();
		bins.add(bin);
		System.out.printf("%d boxes are packed \n", bin.get_boxes().size());
		System.out.printf("time elapsed: %d milliseconds \n", delta_time);
		*/
		// show filling percentage of each bin
		show_filling_percentage(bins);
		// save task and solution to file
		save_results(bins, bin_dimensions);
	}
	
	private static Vector<Place_info> solve_box_sequence(double[] bin_dimensions, Vector<Box> box_sequence, Vector<Bin> bins) {
		bins.clear();
		Placer placer = new Placer();
		Vector<Place_info> how_to_place = placer.place_item(box_sequence, bin_dimensions, bins);
		return how_to_place;
	}
	
	private static Bin solve_single_type_box(double[] bin_dimensions, Box box_template) {
		Placer placer = new Placer();
		return placer.place_item_single_type(box_template, bin_dimensions);
	}
	
	private static double[] define_bin_dimensions() {
		double[] bin_dimensions = new double[3];
		//bin_dimensions[0] = 235;
		//bin_dimensions[1] = 268.7;
		//bin_dimensions[2] = 1203.2;
		bin_dimensions[0] = 50;
		bin_dimensions[1] = 50;
		bin_dimensions[2] = 50;
		return bin_dimensions;
	}
	
	private static Vector<Box> generate_box_sequence() {
		// output stores in the input arguments
		// unit cm
		int count = 3500;
		double box_length_limit = 5;
		Vector<Box> box_sequence = new Vector<Box>();
		for(int i=0; i<count; i++) {
			box_sequence.add(get_random_box(box_length_limit));
			//box_sequence.add(get_single_box());
		}
		Comparator<Box> comparator_size = new Comparator_size();
		Collections.sort(box_sequence, comparator_size);
		return box_sequence;
	}
	
	private static Box get_random_box(double length_limit){
		/*
		double w = Math.random()*length_limit;
		double h = Math.random()*length_limit;
		double d = Math.random()*length_limit;
		*/
		//Random(10);
		double w = Math.ceil(Math.random()*length_limit);
		double h = Math.ceil(Math.random()*length_limit);
		double d = Math.ceil(Math.random()*length_limit);
		
		return new Box(w, h, d);
	}
	
	private static Box get_single_box() {
		/*
		double w = 105;
		double h = 151.5;
		double d = 210;
		*/
		double w = 15;
		double h = 86.4;
		double d = 140;
		Box box = new Box(w, h, d);
		//Box.Orientation[] allowed_ori = {Box.Orientation.Upright};
		//Box.Orientation[] allowed_ori = {Box.Orientation.Upright, Box.Orientation.Side, Box.Orientation.SideRotate, Box.Orientation.Lie};
		Box.Orientation[] allowed_ori = {Box.Orientation.Upright, Box.Orientation.UprightRotate, Box.Orientation.Side, Box.Orientation.SideRotate, Box.Orientation.Lie, Box.Orientation.LieRotate};
		box.set_allowed_ori(allowed_ori);
		return box;
	}
	
	private static void show_boxes_placement(Vector<Box> box_sequence, Vector<Place_info> how_to_place) {
		Place_info place_info = null;
		Box box = null;
		for (int i=0; i<how_to_place.size(); i++) {
			place_info = how_to_place.elementAt(i);
			place_info.show();
			box = box_sequence.elementAt(i).rotate(place_info.best_ori);
			box.show();
		}
	}
	
	private static void show_filling_percentage(Vector<Bin> bins) {
		Vector<Box> boxes;
		Box box;
		double[] dimensions;
		double[] bin_dimensions;
		double bin_volume;
		double box_total_volume;
		double percentage;
		for (int i=0; i<bins.size(); i++) {
			bin_dimensions = bins.elementAt(i).get_dimensions();
			bin_volume = bin_dimensions[0]*bin_dimensions[1]*bin_dimensions[2];
			boxes = bins.elementAt(i).get_boxes();
			box_total_volume = 0;
			for (int j=0; j<boxes.size(); j++) {
				box = boxes.elementAt(j);
				dimensions = box.get_dimensions();
				box_total_volume += dimensions[0]*dimensions[1]*dimensions[2];
			}
			percentage = box_total_volume/bin_volume;
			System.out.printf("bin %d is %f percent filled. \n", i, 100*percentage);
		}
	}
	
	private static void save_results(Vector<Bin> bins, double[] bin_dimensions) {
		// save packing result
		String filename = "result.csv";
		String s;
		Vector<Box> boxes;
		Box box;
		double[] position, dimensions;
		File file = new File(filename);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (int i=0; i<bins.size(); i++) {
				boxes = bins.elementAt(i).get_boxes();
				for (int j=0; j<boxes.size(); j++) {
					box = boxes.elementAt(j);
					position = box.get_position();
					dimensions = box.get_dimensions();
					s = Integer.toString(i)+", " 
					    +Double.toString(position[0])+", " +Double.toString(position[1])+", " +Double.toString(position[2])+", "
					    +Double.toString(dimensions[0])+", " +Double.toString(dimensions[1])+", " +Double.toString(dimensions[2]);
					writer.write(s);
					writer.newLine();
				}
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

class Comparator_size implements Comparator<Box> {
	public int compare(Box left, Box right) {
		double[] dimensions_left = left.get_dimensions();
		double[] dimensions_right = right.get_dimensions();
		//double size_left = dimensions_left[0]+dimensions_left[1]+dimensions_left[2];
		//double size_right = dimensions_right[0]+dimensions_right[1]+dimensions_right[2];
		double size_left = dimensions_left[0]*dimensions_left[0]+dimensions_left[1]*dimensions_left[1]+dimensions_left[2]*dimensions_left[2];
		double size_right = dimensions_right[0]*dimensions_right[0]+dimensions_right[1]*dimensions_right[1]+dimensions_right[2]*dimensions_right[2];
		if (size_left > size_right) {
			return -1;
		}
		if (size_left < size_right) {
			return 1;
		}
		else {
			return 0;
		}
	}
}