package cp_heuristics;

public class Box {
	
	public enum Orientation {
		Upright, UprightRotate, Side, SideRotate, Lie, LieRotate
	}
	
	public Box(double w, double h, double d) {
		this.w = w;
		this.h = h;
		this.d = d;
		this.x = 0;
		this.y = 0;
		this.z = 0;
		Orientation[] allowed_ori = {Orientation.Upright, Orientation.UprightRotate, Orientation.Side, Orientation.SideRotate, Orientation.Lie, Orientation.LieRotate};
		set_allowed_ori(allowed_ori);
		Orientation[] preferred_ori = {Orientation.Upright};
		set_preferred_ori(preferred_ori);
	}
	
	public Box duplicate() {
		Box box = new Box(this.w, this.h, this.d);
		box.set_position(get_position());
		box.set_allowed_ori(allowed_ori);
		box.set_preferred_ori(preferred_ori);
		return box;
	}
	
	public Box rotate(Orientation ori) {
		// check validity
		if (this.is_valid_orientation(ori) == false) {
			System.out.println("prohibited box rotation!");
			return null;
		}
		// start rotation
		double w=-1, h=-1, d=-1;
		switch (ori) {
		case Upright: // no rotation
			w = this.w;
			h = this.h;
			d = this.d;
			break;
		case UprightRotate: // swap w and d
			w = this.d;
			h = this.h;
			d = this.w;
			break;
		case Side: // swap h and w
			w = this.h;
			h = this.w;
			d = this.d;
			break;
		case SideRotate: // w<-d, h<-w, d<-h
			w = this.d;
			h = this.w;
			d = this.h;
			break;
		case Lie: // swap h and d
			w = this.w;
			h = this.d;
			d = this.h;
			break;
		case LieRotate: // w<-h, h<-d, d<-w
			w = this.h;
			h = this.d;
			d = this.w;
			break;
		}
		Box box = new Box(w, h, d);
		box.set_position(this.get_position());
		// for simplicity, box cannot rotate twice
		Orientation[] allowed_ori = {Orientation.Upright};
		box.set_allowed_ori(allowed_ori);
		box.set_preferred_ori(allowed_ori);
		return box;
	}
	
	public boolean is_valid_orientation(Orientation ori) {
		for (Orientation allowed: this.allowed_ori) {
			if (allowed == ori) {
				return true;
			}
		}
		return false;
	}
	
	public void set_allowed_ori(final Orientation[] ori) {
		this.allowed_ori = ori;
	}
	
	public Orientation[] get_allowed_ori() {
		return this.allowed_ori;
	}
	
	public void set_preferred_ori(final Orientation[] ori) {
		this.preferred_ori = ori;
	}
	
	public Orientation[] get_preferred_ori() {
		return this.preferred_ori;
	}
	
	public void set_position(double[] coordinates) {
		this.x = coordinates[0];
		this.y = coordinates[1];
		this.z = coordinates[2];
	}
	
	public double[] get_position() {
		double[] coordinates = new double[3];
		coordinates[0] = this.x;
		coordinates[1] = this.y;
		coordinates[2] = this.z;
		return coordinates;
	}
	
	public double[] get_dimensions() {
		double[] dimensions = new double[3];
		dimensions[0] = this.w;
		dimensions[1] = this.h;
		dimensions[2] = this.d;
		return dimensions;
	}
	
	public double get_shortest_edge() {
		return Math.min(Math.min(this.w, this.h), this.d);
	}
	
	public double get_volume() {
		return this.w*this.h*this.d;
	}
	
	public double get_x_plus_w() {
		return this.x+this.w;
	}
	
	public double get_y_plus_h() {
		return this.y+this.h;
	}
	
	public double get_z_plus_d() {
		return this.z+this.d;
	}
	
	public boolean check_overlap(Box box) {
		// return true if this overlaps with box, otherwise return false
		double[] position = box.get_position();
		double[] dimensions = box.get_dimensions();
		// check first dimension
		double margin = 1e-10;
		if ((x+w-margin <= position[0]) || (x+margin >= position[0]+dimensions[0])) {
			return false;
		}
		// check second dimension
		if ((y+h-margin <= position[1]) || (y+margin >= position[1]+dimensions[1])) {
			return false;
		}
		// check third dimension
		if ((z+d-margin <= position[2]) || (z+margin >= position[2]+dimensions[2])) {
			return false;
		}
		// no overlap found
		return true;
	}
	
	public void show() {
		System.out.printf("box dimensions are: %f, %f, %f \n", this.w, this.h, this.d);
		System.out.printf("box position is: %f, %f, %f \n", this.x, this.y, this.z);
	}
	
	private double x, y, z; // corner position
	private double w, h, d; // dimensions
	private Orientation[] allowed_ori;
	private Orientation[] preferred_ori;
	
}
