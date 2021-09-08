package cp_heuristics;

import java.util.Vector;

public class Utility_functions {
	
	public boolean box_overlap_check(Vector<Box> boxes) {
		// return true if overlap, otherwise false
		Box box1, box2;
		for (int i=0; i<boxes.size(); i++) {
			box1 = boxes.elementAt(i);
			for (int j=i+1; j<boxes.size(); j++) {
				box2 = boxes.elementAt(j);
				if (box1.check_overlap(box2)) { // overlap found
					System.out.printf("boxes %d and %d overlap! \n", i, j);
					box1.show();
					box2.show();
					return true;
				}
			}
		}
		return false;
	}

}
