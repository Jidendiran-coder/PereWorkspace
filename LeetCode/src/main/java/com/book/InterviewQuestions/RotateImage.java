package com.book.InterviewQuestions;

public class RotateImage {

	public static void main(String[] args) {
		
		int[][] matrix = {{1, 2, 3},{4, 5, 6},{7, 8, 9}};
		
		rotate(matrix);
		
	}
	
	public static void rotate(int[][] matrix) {
		
		int n = matrix.length;
	
		for (int i = 0; i < n/2 ; i++) {
			for (int j = i ; j < n-i-1 ; j++) {
				
				int temp = matrix[i][j];
				
				matrix[i][j] = matrix [n - 1 - j][i];
				matrix[n-1-j][i] = matrix[n-1-i][n-1-j];
				matrix[n-1-i][n-1-j] = matrix[j][n-1-i];
				matrix[j][n-1-i] = temp;
				
			}
		}
		
		print(matrix);
	}

	private static void print(int[][] matrix) {
		int N = matrix.length;
		for (int i = 0; i < N; i++)
	    {
	        for (int j = 0; j < N; j++)
	        System.out.print( matrix[i][j] + " ");
	        System.out.println();
	    }
	}
}
