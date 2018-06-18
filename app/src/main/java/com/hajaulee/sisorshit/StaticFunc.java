package com.hajaulee.sisorshit;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

public class StaticFunc {


	public static String captchaToText(Bitmap bc, ArrayList<int[][]>[] trainedData) {
		String captchaText = "";
		Bitmap[] imageArray = new Bitmap[6];
		Log.d("Bitmap size", bc.getWidth() + "x" + bc.getHeight());
		if (bc.getWidth() != 200)
			bc = Bitmap.createScaledBitmap(bc, 200, 80, true);
		// Xoay ảnh captcha 10 độ ngược chiều kim đồng hồ
		Matrix m = new Matrix();
		m.postRotate(-10);
		bc = Bitmap.createBitmap(bc, 0, 0, bc.getWidth(), bc.getHeight(), m, true);

		// Lấy chiều cao của ảnh captcha gốc
		int bcHeight = bc.getHeight();

		// Cắt các ảnh nhỏ của các chữ số trong captcha
		imageArray[1] = Bitmap.createBitmap(bc, 52, 0, 22, bcHeight);
		imageArray[2] = Bitmap.createBitmap(bc, 76, 0, 22, bcHeight);
		imageArray[3] = Bitmap.createBitmap(bc, 95, 0, 22, bcHeight);
		imageArray[4] = Bitmap.createBitmap(bc, 118, 0, 22, bcHeight);
		imageArray[5] = Bitmap.createBitmap(bc, 139, 0, 22, bcHeight);

		//
		// Bỏ phần trắng của ảnh các chữ số
		// Cắt lấy phần trục của các số
		// Đưa ảnh các số về 6x30
		for (int index = 1; index <= 5; index++) {
			int[] pixels = new int[50];
			for (int y = 0; y < 100; y++) {
				// Lấy 1 đoạn thẳng ngang 29x1 có tọa y
				imageArray[index].getPixels(pixels, 0, 50, 0, y, 22, 1);
				// Nếu trên đoạn thẳng ngang có 1 điểm ảnh khác 0
				// Thì cắt ảnh từ tọa độ y đó
				for (int i = 0; i < 22; i++)
					if (pixels[i] != 0) {
						try {
							imageArray[index] = Bitmap.createBitmap(imageArray[index], 0, y,
									imageArray[index].getWidth(), 55);
						} catch (Exception e) {
							Log.e("Erroe crop image", "Cannot get image area");
						}
						imageArray[index] = Bitmap.createScaledBitmap(imageArray[index], 10, 15, true);
						imageArray[index] = Bitmap.createScaledBitmap(imageArray[index], 20, 30, true);
						imageArray[index] = Bitmap.createBitmap(imageArray[index], 7, 0, 6, 30);
						y = 1000;
						break;
					}
			}
		}

		int may_be_digit = 0;
		int curr_max, max_c, sum, width, height;
		// Nhận diện chữ số từ ảnh
		for (int index = 1; index <= 5; index++) {
			curr_max = 0;
			// Tạo mảng int từ ảnh (Tăng tốc độ xử lý tránh dùng getPixels()
			int[][] arrayPixels = new int[6][30];
			for (int x = 0; x < 6; x++)
				for (int y = 0; y < 30; y++)
					arrayPixels[x][y] = imageArray[index].getPixel(x, y);
			// Duyệt từng chữ số từ 0-9
			for (int i = 0; i < 10; i++) {
				max_c = 0;
				if (!trainedData[i].isEmpty()) {
					// Duyệt hình ảnh đã học của số i
					for (int[][] a : trainedData[i]) {
						sum = 0;
						width = a.length;
						height = a[0].length;
						// Tính độ trùng khớp của ảnh hiện tại và ảnh đã học của
						// số i
						for (int x = 0; x < width; x++)
							for (int y = 0; y < height; y++)
								sum += (arrayPixels[x][y] == a[x][y]) ? 1 : 0;
						// Cập nhật lại độ trùng khớp
						max_c = (max_c > sum) ? max_c : sum;
					}
				}
				// Kiểm tra độ trùng khớp của chữ số hiện tại(max_c) đang xét
				// Nếu độ trùng khớp cao hơn những số trước(curr_max) thì cập
				// nhật lại
				// và gán số hiện tại(i) cho giá trị ước đoán (may_be_digit)
				if (max_c > curr_max) {
					curr_max = max_c;
					may_be_digit = i;
				}
				Log.d("Recognition: " + i, "Match: "
						+ 100 * (float) max_c / (imageArray[index].getHeight() * imageArray[index].getWidth()) + "%");
			}

			captchaText += "" + may_be_digit;
		}
		return captchaText;
	}
	
	public static void trainingDataOut(ArrayList<int[][]>[] array, String fileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		Log.d("Pass", "195");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		Log.d("Pass", "197");
		oos.writeObject(array);
		Log.d("Pass", "199");
		oos.close();
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<int[][]>[] loadTrainedData(String fileName)
			throws StreamCorruptedException, IOException, ClassNotFoundException {
		FileInputStream fin = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fin);
		MainActivity.digit = (ArrayList<int[][]>[]) ois.readObject();
		ois.close();
		return MainActivity.digit;
	}


}
