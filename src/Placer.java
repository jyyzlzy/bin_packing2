package cp_heuristics;

import java.util.Vector;

public class Placer {
	
	public Vector<Place_info> place_item(Vector<Box> box_sequence, double[] bin_dimensions, Vector<Bin> bins) {
		// results are stored in bins
		bins.clear();
		Vector<Place_info> how_to_place = new Vector<Place_info>();
		// if no box needs to be placed 
		if (box_sequence.size() == 0) {
			return how_to_place;
		}
		// initialization
		Place_info place_info;
		Bin bin_selected = null;
		Box box = null, box_to_place = null;
		double[] best_ems;
		Box.Orientation best_ori = null;
		int next_bin_id = 0;
		// find the min_w
		int n = box_sequence.size();
		double[] min_w = new double[n];
		double min_w_upper_bound = 1.0+Math.max(Math.max(bin_dimensions[0], bin_dimensions[1]), bin_dimensions[2]);
		min_w[n-1] = min_w_upper_bound;
		for (int i=n-2; i>=0; i--) {
			min_w[i] = Math.min(min_w[i+1], box_sequence.elementAt(i+1).get_shortest_edge());
		}
		// place boxes one by one
		for (int i=0; i<box_sequence.size(); i++) {
			box = box_sequence.elementAt(i);
			// find an existing bin to place the box
			best_ems = new double[3];
			best_ems[0] = -1; // -1 marks invalidity
			for (int j=0; j<bins.size(); j++) {
				best_ori = this.DFTRC(box, bins.elementAt(j), best_ems, "allowed");
				if (best_ems[0] >= 0) {
					bin_selected = bins.elementAt(j);
					break;
				}
			}
			// if no bin is good for the box
			if (best_ems[0] == -1) {
				bin_selected = new Bin(next_bin_id, bin_dimensions);
				next_bin_id++;
				bins.add(bin_selected);
				best_ori = this.DFTRC(box, bin_selected, best_ems, "allowed");
			}
			// update the place information
			place_info = new Place_info();
			place_info.set_ems(best_ems);
			place_info.bin_id = bin_selected.get_id();
			place_info.best_ori = best_ori;
			how_to_place.add(place_info);
			// place the box into the selected bin
			box_to_place = box.rotate(best_ori);
			box_to_place.set_position(best_ems);
			bin_selected.update(box_to_place, min_w[i]);
		}
		// return result
		return how_to_place;
	}
	
	public Bin place_item_single_type(Box box_template, double[] bin_dimensions) {
		// returns a bin that contains as many boxes as possible
		// initialization
		Bin bin = new Bin(0, bin_dimensions);
		Box box = null, box_to_place = null;
		double[] best_ems;
		Box.Orientation best_ori = null;
		// find the min_w
		double min_w = box_template.get_shortest_edge();
		// place boxes with preferred orientations
		while (true) {
			box = box_template.duplicate();
			// find an existing bin to place the box
			best_ems = new double[3];
			best_ems[0] = -1; // -1 marks invalidity
			best_ori = this.DFTRC(box, bin, best_ems, "preferred");
			if (best_ems[0] < 0) { // no where to place
				break;
			}
			// place the box into the selected bin
			box_to_place = box.rotate(best_ori);
			box_to_place.set_position(best_ems);
			bin.update(box_to_place, min_w);
		}
		// place boxes with all allowed orientations
		while (true) {
			box = box_template.duplicate();
			// find an existing bin to place the box
			best_ems = new double[3];
			best_ems[0] = -1; // -1 marks invalidity
			best_ori = this.DFTRC(box, bin, best_ems, "allowed");
			if (best_ems[0] < 0) { // no where to place
				break;
			}
			// place the box into the selected bin
			box_to_place = box.rotate(best_ori);
			box_to_place.set_position(best_ems);
			bin.update(box_to_place, min_w);
		}
		// return result
		return bin;
	}
	
	private Box.Orientation DFTRC(Box box, Bin bin, double[] best_ems, String method) {
		//System.out.println("run DFTRC");
		Box.Orientation best_ori = Box.Orientation.Upright;
		best_ems[0] = -1; // -1 marks an invalid ems
		double dist_sq;
		double max_dist_sq = -1;
		Box box_rotated;
		double[] bin_dimensions, curr_ems;
		double x_plus_w, y_plus_h, z_plus_d, W, H, D;
		Vector<double[]> ems = bin.get_ems();
		bin_dimensions = bin.get_dimensions();
		W = bin_dimensions[0];
		H = bin_dimensions[1];
		D = bin_dimensions[2];
		for (int i=0; i<ems.size(); i++) {
			curr_ems = ems.elementAt(i);
			box.set_position(curr_ems);
			//for (Box.Orientation ori: Box.Orientation.values()) {
			Box.Orientation[] ori_to_search;
			if (method == "preferred") {
				ori_to_search = box.get_preferred_ori();
			}
			else {
				ori_to_search = box.get_allowed_ori();
			}
			for (Box.Orientation ori: ori_to_search) {
				//System.out.printf("%s \n", ori);
				box_rotated = box.rotate(ori);
				//box_rotated.show();
				x_plus_w = box_rotated.get_x_plus_w();
				y_plus_h = box_rotated.get_y_plus_h();
				z_plus_d = box_rotated.get_z_plus_d();
				//System.out.printf("%f, %f, %f \n", x_plus_w, y_plus_h, z_plus_d);
				if ((x_plus_w <= W) && (y_plus_h <= H) && (z_plus_d <= D)) {
					dist_sq = 1*(W-x_plus_w)*(W-x_plus_w) + 1*(H-y_plus_h)*(H-y_plus_h) + 10*(D-z_plus_d)*(D-z_plus_d);
					//box_rotated.show();
					//System.out.printf("%f, %f, %f \n", x_plus_w, y_plus_h, z_plus_d);
					//System.out.printf("%f, %f, %f \n", W-x_plus_w, H-y_plus_h, D-z_plus_d);
					//System.out.printf("dist sq: %f \n", dist_sq);
					if (dist_sq > max_dist_sq) {
						max_dist_sq = dist_sq;
						best_ori = ori;
						//System.out.printf("ems %d, dist_sq %f, ori %s \n", i, dist_sq, best_ori);
						// copy values to best_ems
						best_ems[0] = curr_ems[0];
						best_ems[1] = curr_ems[1];
						best_ems[2] = curr_ems[2];
					}
				}
			}
		}
		return best_ori;
	}
	
	/*
	private double find_min_w(Vector<Box> boxes, int start_idx, double upper_bound) {
		double min_w = upper_bound;
		for (int i=start_idx; i<boxes.size(); i++) {
			min_w = Math.min(min_w, boxes.elementAt(i).get_shortest_edge());
		}
		return min_w;
	}
	*/

}

class Place_info {

	public Place_info() {
		this.best_ems = new double[3];
	}
	
	public void set_ems(double[] ems) {
		this.best_ems[0] = ems[0];
		this.best_ems[1] = ems[1];
		this.best_ems[2] = ems[2];
	}
	
	public void show() {
		System.out.printf("bin id: %d, coordinates: (%f, %f, %f), orientation: %s \n", bin_id, best_ems[0], best_ems[1], best_ems[2], best_ori);
	}
	
	public int bin_id;
	public double[] best_ems;
	public Box.Orientation best_ori;
}
