package cp_heuristics;

import java.util.Collections;
import java.util.Vector;
import java.util.Comparator;
//import java.util.HashSet;

public class Bin {
	
	public Bin(int id, double[] dimensions) {
		this.id = id;
		this.W = dimensions[0];
		this.H = dimensions[1];
		this.D = dimensions[2];
		this.boxes = new Vector<Box>();
		this.ems = new Vector<double[]>();
		double[] origin = {0,0,0};
		this.ems.add(origin);
	}
	
	public int get_id() {
		return this.id;
	}
	
	public double[] get_dimensions() {
		double[] dimensions = new double[3];
		dimensions[0] = this.W;
		dimensions[1] = this.H;
		dimensions[2] = this.D;
		return dimensions;
	}
	
	public double get_longest_edge() {
		return Math.max(Math.max(this.W, this.H), this.D);
	}
	
	public Vector<Box> get_boxes(){
		return this.boxes;
	}
	
	public Vector<double[]> get_ems(){
		return this.ems;
	}
	
	public void update(Box box, double min_w) {
		this.boxes.add(box);
		this.ems = this.three_d_ems(min_w);
	}
	
	private Vector<double[]> two_d_ems(Vector<Box> boxes2d, double min_w) {
		Vector<double[]> corner_points = new Vector<double[]>();
		double[] point;
		// is there is no box in the bin, return origin point
		if (boxes2d.size() == 0) {
			point = new double[2];
			point[0] = 0;
			point[1] = 0;
			corner_points.add(point);
			return corner_points;
		}
		// sort boxes
		Comparator<Box> comparator2d = new Comparator2d();
		Collections.sort(boxes2d, comparator2d);
		// find extreme boxes
		Vector<Box> extreme_boxes = new Vector<Box>();
		Box box;
		double x_bound = 0;
		for (int i=0; i<boxes2d.size(); i++) {
			box = boxes2d.elementAt(i);
			if (box.get_x_plus_w() > x_bound) {
				extreme_boxes.add(box);
				x_bound = box.get_x_plus_w();
			}
		}
		// determine corner points
		// first point
		point = new double[2];
		point[0] = 0;
		point[1] = extreme_boxes.elementAt(0).get_y_plus_h();
		if ((point[0]+min_w <= this.W) && (point[1]+min_w <= this.H)) {
			corner_points.add(point);
		}
		// middle points
		int n = extreme_boxes.size();
		for (int i=1; i<n; i++) {
			point = new double[2];
			point[0] = extreme_boxes.elementAt(i-1).get_x_plus_w();
			point[1] = extreme_boxes.elementAt(i).get_y_plus_h();
			if ((point[0]+min_w <= this.W) && (point[1]+min_w <= this.H)) {
				corner_points.add(point);
			}
		}
		// last point
		point = new double[2];
		point[0] = extreme_boxes.elementAt(n-1).get_x_plus_w();
		point[1] = 0;
		if ((point[0]+min_w <= this.W) && (point[1]+min_w <= this.H)) {
			corner_points.add(point);
		}
		// return
		return corner_points;
	}
	
	private Vector<double[]> three_d_ems(double min_w) {
		Vector<double[]> corner_points = new Vector<double[]>();
		double[] point;
		// empty bin
		if (this.boxes.size() == 0) {
			point = new double[3];
			point[0] = 0;
			point[1] = 0;
			point[2] = 0;
			corner_points.add(point);
			return corner_points;
		};
		// find all distinct z coordinates
		Comparator<Box> comparator = new Comparator3d();
		Collections.sort(this.boxes, comparator);
		Vector<Double> T = new Vector<Double>(); 
		T.add(0.0);
		double z_plus_d, z_plus_d_prev;
		z_plus_d_prev = this.boxes.elementAt(0).get_z_plus_d();
		T.add(z_plus_d_prev);
		for (int i=1; i<this.boxes.size(); i++) {
			z_plus_d = this.boxes.elementAt(i).get_z_plus_d();
			if (z_plus_d != z_plus_d_prev) {
				T.add(z_plus_d);
				z_plus_d_prev = z_plus_d;
			}
		}
		// find corner points
		Vector<double[]> prev_2d_corners = new Vector<double[]>();
		boolean duplicated;
		double[] point2d, prev_point2d, point3d;
		Vector<double[]> two_d_corners;
		Vector<Box> I;
		int idx_box = 0;
		int k = 0;
		while ((k<T.size()) && (T.elementAt(k)+min_w <= this.D)) {
			// increase idx_box until box.z+box.d > T[k] is satisfied
			while ( (idx_box < this.boxes.size()) && (this.boxes.elementAt(idx_box).get_z_plus_d() <= T.elementAt(k)) ) {
				idx_box++;
			}
			// make a subset I
			I = new Vector<Box>();
			for (int i=idx_box; i<this.boxes.size(); i++) {
				I.add(this.boxes.elementAt(i));
			}
			// calculate the 2d corner points
			two_d_corners = two_d_ems(I, min_w);
			// append true corner points to results
			for (int i=0; i<two_d_corners.size(); i++) {
				point2d = two_d_corners.elementAt(i);
				// check if already exist in the previous set
				duplicated = false;
				for (int j=0; j<prev_2d_corners.size(); j++) {
					prev_point2d = prev_2d_corners.elementAt(j);
					if ((point2d[0]==prev_point2d[0]) && (point2d[1]==prev_point2d[1])) {
						duplicated = true;
						break;
					}
				}
				// add to result
				if (duplicated == false) {
					point3d = new double[3];
					point3d[0] = point2d[0];
					point3d[1] = point2d[1];
					point3d[2] = T.elementAt(k);
					corner_points.add(point3d);
				}
			}
			// increase k
			k += 1;
		}
		return corner_points;
	}
	
	private int id;
	private double W, H, D;
    private Vector<Box> boxes;
    private Vector<double[]> ems;

}

class Comparator2d implements Comparator<Box>{
	public int compare(Box left, Box right) {
		// a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
		// check y+h
		double y_plus_h_left = left.get_y_plus_h();
		double y_plus_h_right = right.get_y_plus_h();
		if (y_plus_h_left > y_plus_h_right) {
			return -1;
		}
		if (y_plus_h_left < y_plus_h_right) {
			return 1;
		}
		// break ties with x+w
		double x_plus_w_left = left.get_x_plus_w();
		double x_plus_w_right = right.get_x_plus_w();
		if (x_plus_w_left > x_plus_w_right) {
			return -1;
		}
		if (x_plus_w_left < x_plus_w_right) {
			return 1;
		}
		// exactly the same
		return 0;
	}
}

class Comparator3d implements Comparator<Box>{
	public int compare(Box left, Box right) {
		double z_plus_d_left = left.get_z_plus_d();
		double z_plus_d_right = right.get_z_plus_d();
		if (z_plus_d_left < z_plus_d_right) {
			return -1;
		}
		if (z_plus_d_left > z_plus_d_right) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
