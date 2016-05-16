package org.antlr.codebuff.kdtree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

//This is a java program to find nearest neighbor using KD Tree implementation
public class Play {
	static class KDNode {
		int axis;
		double[] x;
		int id;
		boolean checked;
		boolean orientation;

		KDNode Parent;
		KDNode Left;
		KDNode Right;

		public KDNode(double[] x0, int axis0) {
			x = new double[2];
			axis = axis0;
			for (int k = 0; k<2; k++)
				x[k] = x0[k];

			Left = Right = Parent = null;
			checked = false;
			id = 0;
		}

		public KDNode FindParent(double[] x0) {
			KDNode parent = null;
			KDNode next = this;
			int split;
			while ( next!=null ) {
				split = next.axis;
				parent = next;
				if ( x0[split]>next.x[split] )
					next = next.Right;
				else
					next = next.Left;
			}
			return parent;
		}

		public KDNode Insert(double[] p) {
			//x = new double[2];
			KDNode parent = FindParent(p);
			if ( equal(p, parent.x, 2)==true )
				return null;

			KDNode newNode = new KDNode(p, parent.axis+1<2 ? parent.axis+1
				: 0);
			newNode.Parent = parent;

			if ( p[parent.axis]>parent.x[parent.axis] ) {
				parent.Right = newNode;
				newNode.orientation = true; //
			}
			else {
				parent.Left = newNode;
				newNode.orientation = false; //
			}

			return newNode;
		}

		boolean equal(double[] x1, double[] x2, int dim) {
			for (int k = 0; k<dim; k++) {
				if ( x1[k]!=x2[k] )
					return false;
			}

			return true;
		}

		double distance2(double[] x1, double[] x2, int dim) {
			double S = 0;
			for (int k = 0; k<dim; k++)
				S += (x1[k]-x2[k])*(x1[k]-x2[k]);
			return S;
		}
	}

	static class KDTree {
		KDNode Root;

		int TimeStart, TimeFinish;
		int CounterFreq;

		double d_min;
		KDNode nearest_neighbour;

		int KD_id;

		int nList;

		KDNode CheckedNodes[];
		int checked_nodes;
		KDNode List[];

		double x_min[], x_max[];
		boolean max_boundary[], min_boundary[];
		int n_boundary;

		public KDTree(int i) {
			Root = null;
			KD_id = 1;
			nList = 0;
			List = new KDNode[i];
			CheckedNodes = new KDNode[i];
			max_boundary = new boolean[2];
			min_boundary = new boolean[2];
			x_min = new double[2];
			x_max = new double[2];
		}

		public boolean add(double[] x) {
			if ( nList>=2000000-1 )
				return false; // can't add more points

			if ( Root==null ) {
				Root = new KDNode(x, 0);
				Root.id = KD_id++;
				List[nList++] = Root;
			}
			else {
				KDNode pNode;
				if ( (pNode = Root.Insert(x))!=null ) {
					pNode.id = KD_id++;
					List[nList++] = pNode;
				}
			}

			return true;
		}

		public KDNode find_nearest(double[] x) {
			if ( Root==null )
				return null;

			checked_nodes = 0;
			KDNode parent = Root.FindParent(x);
			nearest_neighbour = parent;
			d_min = Root.distance2(x, parent.x, 2);
			;

			if ( parent.equal(x, parent.x, 2)==true )
				return nearest_neighbour;

			search_parent(parent, x);
			uncheck();

			return nearest_neighbour;
		}

		public void check_subtree(KDNode node, double[] x) {
			if ( (node==null) || node.checked )
				return;

			CheckedNodes[checked_nodes++] = node;
			node.checked = true;
			set_bounding_cube(node, x);

			int dim = node.axis;
			double d = node.x[dim]-x[dim];

			if ( d*d>d_min ) {
				if ( node.x[dim]>x[dim] )
					check_subtree(node.Left, x);
				else
					check_subtree(node.Right, x);
			}
			else {
				check_subtree(node.Left, x);
				check_subtree(node.Right, x);
			}
		}

		public void set_bounding_cube(KDNode node, double[] x) {
			if ( node==null )
				return;
			int d = 0;
			double dx;
			for (int k = 0; k<2; k++) {
				dx = node.x[k]-x[k];
				if ( dx>0 ) {
					dx *= dx;
					if ( !max_boundary[k] ) {
						if ( dx>x_max[k] )
							x_max[k] = dx;
						if ( x_max[k]>d_min ) {
							max_boundary[k] = true;
							n_boundary++;
						}
					}
				}
				else {
					dx *= dx;
					if ( !min_boundary[k] ) {
						if ( dx>x_min[k] )
							x_min[k] = dx;
						if ( x_min[k]>d_min ) {
							min_boundary[k] = true;
							n_boundary++;
						}
					}
				}
				d += dx;
				if ( d>d_min )
					return;

			}

			if ( d<d_min ) {
				d_min = d;
				nearest_neighbour = node;
			}
		}

		public KDNode search_parent(KDNode parent, double[] x) {
			for (int k = 0; k<2; k++) {
				x_min[k] = x_max[k] = 0;
				max_boundary[k] = min_boundary[k] = false; //
			}
			n_boundary = 0;

			KDNode search_root = parent;
			while ( parent!=null && (n_boundary!=2*2) ) {
				check_subtree(parent, x);
				search_root = parent;
				parent = parent.Parent;
			}

			return search_root;
		}

		public void uncheck() {
			for (int n = 0; n<checked_nodes; n++)
				CheckedNodes[n].checked = false;
		}

	}

	public static void main(String args[]) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("input.txt"));
		int numpoints = 5;

		KDTree kdt = new KDTree(numpoints);
		double x[] = new double[2];

		x[0] = 2.1;
		x[1] = 4.3;
		kdt.add(x);

		x[0] = 3.3;
		x[1] = 1.5;
		kdt.add(x);

		x[0] = 4.7;
		x[1] = 11.1;
		kdt.add(x);

		x[0] = 5.0;
		x[1] = 12.3;
		kdt.add(x);

		x[0] = 5.1;
		x[1] = 1.2;
		kdt.add(x);

		System.out
			.println("Enter the co-ordinates of the point: (one after the other)");
		InputStreamReader reader = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(reader);
		double sx = Double.parseDouble(br.readLine());
		double sy = Double.parseDouble(br.readLine());

		double s[] = {sx, sy};
		KDNode kdn = kdt.find_nearest(s);
		System.out.println("The nearest neighbor is: ");
		System.out.println("("+kdn.x[0]+" , "+kdn.x[1]+")");
		in.close();
	}
}
