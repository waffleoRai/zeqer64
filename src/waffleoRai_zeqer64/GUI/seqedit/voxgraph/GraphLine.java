package waffleoRai_zeqer64.GUI.seqedit.voxgraph;

import java.awt.Color;
import java.util.ArrayList;

public class GraphLine {
	
	public static final int LINE_TYPE_PLOT = 0;
	public static final int LINE_TYPE_VERT = 1;
	public static final int LINE_TYPE_HORZ = 2;
	
	public static class GraphPoint implements Comparable<GraphPoint>{
		public double x;
		public double y;
		
		public int compareTo(GraphPoint o) {
			if(o == null) return 1;
			if(this.x > o.x) return 1;
			if(o.x > this.x) return -1;
			if(this.y > o.y) return 1;
			if(o.y > this.y) return -1;
			return 0;
		}
		
		
	}
	
	/*--- Instance Variables ---*/
	
	public int type = LINE_TYPE_PLOT;
	public float thickness = 1.0f;
	public Color lineColor = Color.black;
	
	public double constVal = 0;
	public ArrayList<GraphPoint> points;

	public GraphLine() {
		points = new ArrayList<GraphPoint>();
	}
	
}
