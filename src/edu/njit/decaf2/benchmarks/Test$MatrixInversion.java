/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2.benchmarks;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholder;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderTran;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import org.ejml.alg.dense.linsol.lu.LinearSolverLuKJI;
import org.ejml.alg.dense.linsol.qr.LinearSolverQr;
import org.ejml.alg.dense.linsol.svd.SolvePseudoInverseSvd;
import org.ejml.data.DenseMatrix64F;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.MatrixBuilder;
import org.ojalgo.matrix.MatrixFactory;
import org.ojalgo.matrix.PrimitiveMatrix;

import Jama.Matrix;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;

/**
 * DECAF - Test$MatrixInversion
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class Test$MatrixInversion {
	public static void main(String[] args) {
		double[][] qmatrix = Test$PMatrices.grab(640);
		DenseMatrix64F matrix = new DenseMatrix64F(qmatrix);
		DenseMatrix64F[] results = new DenseMatrix64F[6];

		System.out.println("\nOJALGO Benchmarks");
		/** ----------------- Invert ----------------- **/
		double t = System.nanoTime();
		MatrixFactory<?> tmpFactory = PrimitiveMatrix.FACTORY;
		MatrixBuilder<?> tmpBuilder = tmpFactory.getBuilder(18, 18);
		for (int j = 0; j < tmpBuilder.getColDim(); j++) {
			for (int i = 0; i < tmpBuilder.getRowDim(); i++) {
				tmpBuilder.set(i, j, qmatrix[i][j]);
			}
		}
		BasicMatrix tmpI = tmpBuilder.build();
		tmpI.invert();
		System.out.println("OJALGO Inverse:            " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		System.out.println("\nJAMA Benchmarks");
		/** ----------------- Invert ----------------- **/
		t = System.nanoTime();
		Matrix m = new Matrix(qmatrix);
		m.inverse();
		System.out.println("JAMA Inverse:            " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		System.out.println("\nEJML Benchmarks");
		/** ----------------- SolvePseudoInverseSvd ----------------- **/
		t = System.nanoTime();

		SolvePseudoInverseSvd spisvd = new SolvePseudoInverseSvd();
		results[0] = matrix.copy();
		spisvd.setA(results[0]);
		spisvd.invert(results[0]);

		System.out.println("Pseudo-inverse SVD:        " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		/**
		 * ----------------- LUDecompositionAlt + LinearSolverLuKJI -----------------
		 **/
		t = System.nanoTime();

		LUDecompositionAlt ludbalt = new LUDecompositionAlt();
		ludbalt.decompose(matrix.copy());
		LinearSolverLuKJI lukji = new LinearSolverLuKJI(ludbalt);
		results[1] = matrix.copy();
		lukji.setA(results[1]);
		lukji.invert(results[1]);

		System.out.println("LUDecomp + LUKJI-Inverse:  " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		/**
		 * ----------------- LUDecompositionAlt + LinearSolverLu -----------------
		 **/
		t = System.nanoTime();

		LinearSolverLu lslu = new LinearSolverLu(ludbalt);
		results[2] = matrix.copy();
		lslu.setA(results[2]);
		lslu.invert(results[2]);

		System.out.println("LUDecomp + LU Inverse:     " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		/**
		 * ----------------- QRDecompositionHouseholder + LinearSolverQr -----------------
		 **/
		t = System.nanoTime();

		QRDecompositionHouseholder qrhh = new QRDecompositionHouseholder();
		qrhh.decompose(matrix.copy());
		LinearSolverQr alsqr = new LinearSolverQr(qrhh);
		results[3] = matrix.copy();
		alsqr.setA(results[3]);
		alsqr.invert(results[3]);

		System.out.println("QRHouse + QRSolver:        " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		/**
		 * ----------------- QRDecompositionHouseholderTran + LinearSolverQr -----------------
		 **/
		t = System.nanoTime();

		QRDecompositionHouseholderTran qrht = new QRDecompositionHouseholderTran();
		qrht.decompose(matrix.copy());
		alsqr = new LinearSolverQr(qrht);
		results[4] = matrix.copy();
		alsqr.setA(results[4]);
		alsqr.invert(results[4]);

		System.out.println("QRHouseTran + QRSolver:    " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		/**
		 * ----------------- QRDecompositionHouseholderColumn + LinearSolverQr -----------------
		 **/
		t = System.nanoTime();

		QRDecompositionHouseholderColumn qrhc = new QRDecompositionHouseholderColumn();
		qrhc.decompose(matrix.copy());
		alsqr = new LinearSolverQr(qrhc);
		results[5] = matrix.copy();
		alsqr.setA(results[5]);
		alsqr.invert(results[5]);

		System.out.println("QRHouseColm + QRSolver:    " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");

		/**
		 * ----------------- Parallel Colt Inverse -----------------
		 **/
		t = System.nanoTime();
		DenseDoubleMatrix2D colt = new DenseDoubleMatrix2D(qmatrix);
		DenseDoubleAlgebra da = new DenseDoubleAlgebra();
		da.inverse(colt);
		System.out.println("Parallel Colt Inverse:    " + (System.nanoTime() - t) / 1000.0 / 1000.0 / 1000.0 + " s");
	}
}
