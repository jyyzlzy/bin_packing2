package cp_heuristics;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class Batch_Placement {
	
	public enum Plane {
		Bottom, Side, Back
	}
	
	public Vector<Box> test_fill_layer(Box box, double[] bin_dimensions) {
		// fill the back layer
		double[] box_dimensions = box.get_dimensions();
		double w = box_dimensions[0];
		double h = box_dimensions[1];
		int[] fill_results = fill_layer(bin_dimensions[0], bin_dimensions[1], w, h);
		// print fill results
		for (int i=0; i<9; i++) {
			System.out.printf("%d, ", fill_results[i]);
		}
		System.out.printf("\n");
		// write a layer_info 
		Layer_info layer_info = new Layer_info();
		layer_info.fill_percentage = fill_results[0]*box_dimensions[0]*box_dimensions[1]/bin_dimensions[0]/bin_dimensions[1];
		for (int i=0; i<8; i++) {
			layer_info.blocks[i] = fill_results[i+1];
		}
		layer_info.wall = Plane.Back;
		Box.Orientation[] directions = {Box.Orientation.Upright, Box.Orientation.Side};
		layer_info.modify_directions(directions);
		layer_info.box_height = box_dimensions[2];
		// place boxes 
		double[] box_position = new double[3];
		box_position[2] = 0;
		double[] origin = new double[3];
		origin[0] = 0;
		origin[1] = 0;
		origin[2] = 0;
		double[] space_dimensions = new double[3];
		space_dimensions[0] = bin_dimensions[0];
		space_dimensions[1] = bin_dimensions[1];
		space_dimensions[2] = bin_dimensions[2];
		int ix = 0, iy = 1, iz = 2;
		Vector<Box> boxes = new Vector<Box>();
		place_boxes_into_layer(boxes, box, layer_info, origin, space_dimensions, ix, iy, iz);
		// return
		return boxes;
	}
	
	public Vector<Box> fill_bin_single_pass(Box box_template, double[] bin_dimensions) {
		//Bin bin = new Bin(0, bin_dimensions);
		Vector<Box> boxes = new Vector<Box>();
		double[] origin = new double[3];
		origin[0] = 0;
		origin[1] = 1;
		origin[2] = 2;
		double[] space_dimensions = new double[3];
		space_dimensions[0] = bin_dimensions[0];
		space_dimensions[1] = bin_dimensions[1];
		space_dimensions[2] = bin_dimensions[2];
		Layer_info layer_info;
		int ix=-1, iy=-1, iz=-1;
		while (true) {
			layer_info = find_best_layer(box_template, space_dimensions);
			// no space to place
			if (layer_info.fill_percentage == 0.0) {
				break;
			}
			// project axis
			switch (layer_info.wall) {
			case Bottom:
				ix = 0;
				iy = 2;
				iz = 1;
				break;
			case Side:
				ix = 2;
				iy = 1;
				iz = 0;
				break;
			case Back:
				ix = 0;
				iy = 1;
				iz = 2;
				break;
			}
			// place boxes into the bin and update origin and space dimensions
			place_boxes_into_layer(boxes, box_template, layer_info, origin, space_dimensions, ix, iy, iz);
		}
		// return
		return boxes;
	}
	
	public Vector<Box> fill_bin_exhaustive_search(Box box_template, double[] bin_dimensions){
		
		// initialization
		Vector<Layer_info> best_placement = new Vector<Layer_info>();
		Vector<Layer_info> curr_placement = new Vector<Layer_info>();
		double[] origin = new double[3];
		double[] space_dimensions = new double[3];
		for (int i=0; i<3; i++) {
			origin[i] = 0;
			space_dimensions[i] = bin_dimensions[i];
		}
		int[] best_number = new int[1];
		best_number[0] = 0;
		int[] curr_number = new int[1];
		curr_number[0] = 0;
		// tree search
		backtracking(box_template, best_placement, curr_placement, origin, space_dimensions, best_number, curr_number);
		System.out.println("tree search finished");
		// place layers
		Layer_info layer_info;
		int ix=-1, iy=-1, iz=-1;
		for (int i=0; i<3; i++) {
			origin[i] = 0;
			space_dimensions[i] = bin_dimensions[i];
		}
		Vector<Box> boxes = new Vector<Box>();
		for (int i=0; i<best_placement.size(); i++) {
			layer_info = best_placement.elementAt(i);
			// no space to place
			if (layer_info.fill_percentage == 0.0) {
				continue;
			}
			// project axis
			switch (layer_info.wall) {
			case Bottom:
				ix = 0;
				iy = 2;
				iz = 1;
				break;
			case Side:
				ix = 2;
				iy = 1;
				iz = 0;
				break;
			case Back:
				ix = 0;
				iy = 1;
				iz = 2;
				break;
			}
			// place boxes into the bin and update origin and space dimensions
			place_boxes_into_layer(boxes, box_template, layer_info, origin, space_dimensions, ix, iy, iz);
			//System.out.printf("%d boxes packed \n", boxes.size());
		}
		/*
		// 
		System.out.printf("there are %d layers, they are \n", best_placement.size());
		for (int i=0; i<best_placement.size(); i++) {
			best_placement.elementAt(i).show();
		}
		System.out.printf("\n");
		*/
		// return
		return boxes;
	}
	
	private void backtracking(Box box_template, Vector<Layer_info> best_placement, Vector<Layer_info> curr_placement, 
			double[] origin, double[] space_dimensions, int[] best_number, int[] curr_number){
		
		// backtracking: when no enough space left
		if (space_dimensions[0]*space_dimensions[1]*space_dimensions[2] <= box_template.get_volume()*(best_number[0]-curr_number[0])) {
			return;
		}
		// run fill_layer for all possible walls and directions
		Vector<Layer_info> children = check_all_layers(box_template, space_dimensions);
		// when a leaf is reached 
		if ((children.size() == 0) || (curr_placement.size() >= 25)) {
			if (curr_number[0] > best_number[0]) {
				best_number[0] = curr_number[0];
				best_placement.clear();
				for (int i=0; i< curr_placement.size(); i++) {
					best_placement.add(curr_placement.elementAt(i));
				}
				// count how many boxes are in curr_placement
				int count = 0;
				for (int k=0; k<best_placement.size(); k++) {
					count += best_placement.elementAt(k).get_count();
				}
				// end counting
				System.out.printf("tree depth is %d, boxes packed %d, %d \n", curr_placement.size(), curr_number[0], count);
			}
			return;
		}
		// exploration
		Comparator<Layer_info> comparator = new Comparator_layer_info();
		Collections.sort(children, comparator);
		Layer_info layer_info;
		for (int i=0; (i<children.size()) && (i<2); i++) {
			layer_info = children.elementAt(i);
			int iz = -1;
			switch (layer_info.wall) {
			case Bottom:
				iz = 1;
				break;
			case Side:
				iz = 0;
				break;
			case Back:
				iz = 2;
				break;
			}
			// move forward
			curr_placement.add(layer_info);
			curr_number[0] += layer_info.get_count(); 
			origin[iz] += layer_info.box_height;
			space_dimensions[iz] -= layer_info.box_height;
			// recurse
			backtracking(box_template, best_placement, curr_placement, origin, space_dimensions, best_number, curr_number);
			// move backward
			curr_placement.remove(curr_placement.size()-1);
			curr_number[0] -= layer_info.get_count();
			origin[iz] -= layer_info.box_height;
			space_dimensions[iz] += layer_info.box_height;
		}
	}
	
	private Layer_info find_best_layer(Box box_template, double[] space_dimensions) {
		Layer_info layer_info = new Layer_info();
		Vector<Layer_info> layer_info_list = check_all_layers(box_template, space_dimensions);
		for (int i=0; i< layer_info_list.size(); i++) {
			if (layer_info.fill_percentage < layer_info_list.elementAt(i).fill_percentage) {
				layer_info = layer_info_list.elementAt(i);
			}
		}
		return layer_info;
	}
	
	private void get_projected_dimensions(Plane wall, double[] space_dimensions, double[] space_dimensions_2d) {
		// results are stored in space_dimensions_2d
		switch (wall) {
		case Bottom:
			space_dimensions_2d[0] = space_dimensions[0];
			space_dimensions_2d[1] = space_dimensions[2];
			space_dimensions_2d[2] = space_dimensions[1]; // height is also used
			break;
		case Side:
			space_dimensions_2d[0] = space_dimensions[2];
			space_dimensions_2d[1] = space_dimensions[1];
			space_dimensions_2d[2] = space_dimensions[0]; // height is also used
			break;
		case Back:
			space_dimensions_2d[0] = space_dimensions[0];
			space_dimensions_2d[1] = space_dimensions[1];
			space_dimensions_2d[2] = space_dimensions[2]; // height is also used
			break;
		default:
			System.out.println("warning: invalid plane");	
		}
	}
	
	private Vector<Box.Orientation[]> get_valid_direction_pairs(Plane wall, Box box) {
		/*
		direction pairs are:
			bottom
			Upright, UprightRotate: 0,1
			Side, SideRotate: 2,3
			Lie, LieRotate: 4,5
			
			side
			Upright, Lie: 0,4
			Side, LieRotate: 2,5
			UprightRotate, SideRotate: 1,3
			
			back
			Upright, Side: 0,2
			Lie, SideRotate: 4,3
			UprightRotate, LieRotate: 1,5
		 */
		
		// mark all available directions
		boolean[] valid = new boolean[6];
		Box.Orientation[] all_orientations = {Box.Orientation.Upright, Box.Orientation.UprightRotate, 
				                              Box.Orientation.Side, Box.Orientation.SideRotate, 
				                              Box.Orientation.Lie, Box.Orientation.LieRotate};
		for (int i=0; i<6; i++) {
			if (box.is_valid_orientation(all_orientations[i])) {
				valid[i] = true;
			}
			else {
				valid[i] = false;
			}
		}
		// build valid direction pairs
		Vector<Box.Orientation[]> valid_directions = new Vector<Box.Orientation[]>();
		Box.Orientation[] direction_pair;
		int[] pairs = new int[6];
		int first, second;
		switch (wall) {
		case Bottom:
			pairs[0] = 0;
			pairs[1] = 1;
			pairs[2] = 2;
			pairs[3] = 3;
			pairs[4] = 4;
			pairs[5] = 5;
			break;
		case Side:
			pairs[0] = 0;
			pairs[1] = 4;
			pairs[2] = 2;
			pairs[3] = 5;
			pairs[4] = 1;
			pairs[5] = 3;
			break;
		case Back:
			pairs[0] = 0;
			pairs[1] = 2;
			pairs[2] = 4;
			pairs[3] = 3;
			pairs[4] = 1;
			pairs[5] = 5;
			break;
		default:
			System.out.println("warning: invalid plane");
			return valid_directions;
		}
		// use pairs to build output
		for (int i=0; i<3; i++) {
			// iterate through pairs
			first = pairs[2*i];
			second = pairs[2*i+1];
			if (valid[first]) {
				if (valid[second]) {
					direction_pair = new Box.Orientation[2]; 
					direction_pair[0] = all_orientations[first];
					direction_pair[1] = all_orientations[second];
					valid_directions.add(direction_pair);
				}
				else {
					direction_pair = new Box.Orientation[1];
					direction_pair[0] = all_orientations[first];
					valid_directions.add(direction_pair);
				}
			}
			else {
				if (valid[second]) {
					direction_pair = new Box.Orientation[1];
					direction_pair[0] = all_orientations[second];
					valid_directions.add(direction_pair);
				}
				else {
					; // no valid directions to add for this pair
				}
			}
		}
		// return 
		return valid_directions;
	}
	
	private int[] fill_layer(double L, double W, double l, double w) {
		// this function use the notations in the paper
		// L and l corresponds to x axis, W and w corresponds to y asix
		// return F_max, a0, b0, m0, n0, u0, v0, x0, y0
		// initialization
		int[] results = new int[9];
		int a_max = (int) Math.floor(L/l);
		int b_max = (int) Math.floor(W/w);
		int n_max = (int) Math.floor(W/l);
		int u_max = (int) Math.floor(L/l);
		int a, b, m, n, u, v, x, y; 
		int F = a_max*b_max;
		results[0] = F;
		results[1] = a_max;
		results[2] = b_max;
		for (int i=3; i<9; i++) {
			results[i] = 0;
		}
		// iterate all possible combinations
		for (a=0; a<=a_max; a++) {
			m = (int) Math.floor((L-a*l)/w);
			for (b=1; b<=b_max; b++) {
				if (a==0) {
					b = 0;
				}
				for (n=(int)Math.ceil(b*w/l); n<n_max; n++) {
					if (m == 0) {
						n = 0;
						v = (int) Math.floor((W-b*w)/w); // in this special case, v is limited by b
					}
					else {
						v = (int) Math.floor((W-n*l)/w); // in general, v is limited by n
					}
					for (u=(int)Math.ceil(m*w/l); u<u_max; u++) {
						if (v == 0) {
							u = 0;
							x = (int) Math.floor((L-m*w)/w); // in this special case, x is limited by n
						}
						else {
							x = (int) Math.floor((L-u*l)/w); // in general, x is limited by v
						}
						y = (int) Math.floor((W-b*w)/l);
						if (x*y == 0) {
							x = 0;
							y = 0;
						}
						F = a*b+m*n+u*v+x*y;
						if (F > results[0]) {
							results[0] = F;
							results[1] = a;
							results[2] = b;
							results[3] = m;
							results[4] = n;
							results[5] = u;
							results[6] = v;
							results[7] = x;
							results[8] = y;
						}
						if (v == 0) {
							break;
						}
					}
					if (m == 0) {
						break;
					}
				}
				if (a==0) {
					break;
				}
			}
		}
		// return
		return results;
	}
	
	private Vector<Layer_info> check_all_layers(Box box_template, double[] space_dimensions) {
		Vector<Layer_info> layer_info_list = new Vector<Layer_info>();
		Box.Orientation[] directions;
		Layer_info layer_info;
		double[] space_dimensions_projected = new double[3];
		double[] box_dimensions_projected = new double[3];
		for (Plane wall: Plane.values()) {
			get_projected_dimensions(wall, space_dimensions, space_dimensions_projected);
			Vector<Box.Orientation[]> valid_directions = get_valid_direction_pairs(wall, box_template);
			for (int i=0; i<valid_directions.size(); i++) {
				directions = valid_directions.elementAt(i);
				// invalid direction
				if (directions.length == 0) {
					continue;
				}
				// project box dimensions to box_dimensions_projected
				get_projected_dimensions(wall, box_template.rotate(directions[0]).get_dimensions(), box_dimensions_projected);
				// too high
				if (box_dimensions_projected[2] > space_dimensions_projected[2]) {
					continue;
				}
				// rename some variables to be short
				double X_length = space_dimensions_projected[0];
				double Y_length = space_dimensions_projected[1];
				double x_length = box_dimensions_projected[0];
				double y_length = box_dimensions_projected[1];
				layer_info = new Layer_info();
				if (directions.length == 1) {
					// contain 1 vaild direction
					int a = (int) Math.floor(X_length/x_length);
					int b = (int) Math.floor(Y_length/y_length); 
					layer_info.fill_percentage = a*b*x_length*y_length/X_length/Y_length;;
					layer_info.wall = wall;
					layer_info.modify_directions(directions);
					if (a*b != 0) {
						layer_info.blocks[0] = a;
						layer_info.blocks[1] = b;
					}
					for (int k=2; k<8; k++) {
						layer_info.blocks[k] = 0;
					}
				}
				else if (directions.length == 2){
					// contain 2 valid directions
					int[] fill_2d = fill_layer(X_length, Y_length, x_length, y_length);
					layer_info.fill_percentage = fill_2d[0]*x_length*y_length/X_length/Y_length;
					layer_info.wall = wall;
					layer_info.modify_directions(directions);
					for (int k=0; k<8; k++) {
						layer_info.blocks[k] = fill_2d[k+1];
					}
				}
				else {
					System.out.println("Error: contain more than 2 directions");
					continue;
				}
				// only append layer info if at least 1 box is contained
				if (layer_info.get_count()> 0) {
					layer_info.box_height = box_dimensions_projected[2];
					layer_info_list.add(layer_info);
				}
			}
		}
		return layer_info_list;
	}
	
	private void place_boxes_into_layer(Vector<Box> boxes, Box box_template, Layer_info layer_info, double[] origin, double[] space_dimensions, int ix, int iy, int iz) {
		
		double[] box_position = new double[3];
		box_position[iz] = origin[iz];
		double[] box_dimensions = box_template.rotate(layer_info.directions[0]).get_dimensions();
		// place area a b (indexes 0 and 1)
		double origin_x = origin[ix];
		double origin_y = origin[iy];
		Box new_box;
		for (int i=0; i<layer_info.blocks[0]; i++) {
			for (int j=0; j<layer_info.blocks[1]; j++) {
				new_box = box_template.rotate(layer_info.directions[0]);
				box_position[ix] = origin_x + i*box_dimensions[ix];
				box_position[iy] = origin_y + j*box_dimensions[iy];
				new_box.set_position(box_position);
				boxes.add(new_box);
			}
		}
		if (layer_info.directions.length == 2) {
			// place area m n (indexes 2 and 3)
			origin_x = origin[ix] + layer_info.blocks[0]*box_dimensions[ix];
			origin_y = origin[iy];
			for (int i=0; i<layer_info.blocks[2]; i++) {
				for (int j=0; j<layer_info.blocks[3]; j++) {
					new_box = box_template.rotate(layer_info.directions[1]);
					box_position[ix] = origin_x + i*box_dimensions[iy];
					box_position[iy] = origin_y + j*box_dimensions[ix];
					new_box.set_position(box_position);
					boxes.add(new_box);
				}
			}
			// place area u v (indexes 4 and 5)
			origin_x = origin[ix] + layer_info.blocks[6]*box_dimensions[iy];
			if (layer_info.blocks[3] != 0) {
				origin_y = layer_info.blocks[3]*box_dimensions[ix]; // in general, limited by n
			}
			else {
				origin_y = layer_info.blocks[1]*box_dimensions[iy]; // if n == 0, limited by b
			}
			for (int i=0; i<layer_info.blocks[4]; i++) {
				for (int j=0; j<layer_info.blocks[5]; j++) {
					new_box = box_template.rotate(layer_info.directions[0]);
					box_position[ix] = origin_x + i*box_dimensions[ix];
					box_position[iy] = origin_y + j*box_dimensions[iy];
					new_box.set_position(box_position);
					boxes.add(new_box);
				}
			}
			// place area x y (indexes 6 and 7)
			origin_x = origin[ix];
			origin_y = origin[iy] + layer_info.blocks[1]*box_dimensions[iy];
			for (int i=0; i<layer_info.blocks[6]; i++) {
				for (int j=0; j<layer_info.blocks[7]; j++) {
					new_box = box_template.rotate(layer_info.directions[1]);
					box_position[ix] = origin_x + i*box_dimensions[iy];
					box_position[iy] = origin_y + j*box_dimensions[ix];
					new_box.set_position(box_position);
					boxes.add(new_box);
				}
			}
		}
		// update origin and space_dimensions
		origin[iz] += box_dimensions[iz];
		space_dimensions[iz] -= box_dimensions[iz];
	}
		
}

class Layer_info {
	
	public Layer_info() {
		this.fill_percentage = 0.0;
		wall = null;
		directions = null;
		blocks = new int[8];
		box_height = 0;
	}
	
	public void modify_directions(Box.Orientation[] directions) {
		this.directions = new Box.Orientation[directions.length];
		for (int i=0; i<directions.length; i++) {
			this.directions[i] = directions[i];
		}
	}
	
	public int get_count() {
		return blocks[0]*blocks[1]+blocks[2]*blocks[3]+blocks[4]*blocks[5]+blocks[6]*blocks[7];
	}
	
	public void show_blocks() {
		for (int i=0; i<8; i++) {
			System.out.printf("%d, ", blocks[i]);
		}
		System.out.printf("\n");
	}
	
	public void show() {
		System.out.printf("layer directions is %s, height is %f \n", wall, box_height);
		show_blocks();
	}
	
	public double fill_percentage;
	public Batch_Placement.Plane wall;
	public Box.Orientation[] directions; // length can be 0(no placement), 1(only direction), 2(both directions can be extracted); 
	public int[] blocks;
	public double box_height;
}

class Comparator_layer_info implements Comparator<Layer_info>{
	public int compare(Layer_info left, Layer_info right) {
		if (left.fill_percentage > right.fill_percentage) {
			return -1;
		}
		else if (left.fill_percentage < right.fill_percentage) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
