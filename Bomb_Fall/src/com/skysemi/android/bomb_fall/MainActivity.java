package com.skysemi.android.bomb_fall;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView scoreView;
	private SurfaceView sv;

	private Thread thread;
	private int width;
	private int height;

	private int playerRadius;
	private Paint playerPaint;
	private int playerX;
	private int playerY;
	private int movePlayer;

	private int fallObjRadius;
	private Paint fallObjPaint;
	private int fallObjX;
	private int fallObjY;

	private int bombRadius;
	private Bitmap bm;
	private static final float BOMB_RATE = 0.2f;

	private ArrayList<FallObject> objectArray;
	private static final int OBJECT_DEF_DELAY = 20;
	private int objectDelay = 0;

	private int score = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		scoreView = (TextView)findViewById(R.id.score);
		sv = (SurfaceView)findViewById(R.id.surfaceView);
		bm = BitmapFactory.decodeResource(getResources(), R.drawable.bomb);
		init();

	}

	public void init() {
		sv.getHolder().addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				thread = null;

			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				draw(holder);

			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {

			}
		});
	}

	private void draw(SurfaceHolder holder) {
		width = sv.getWidth();
		height = sv.getHeight();

		//プレイヤー設定
		playerRadius = width / 20;
		playerPaint = new Paint();
		playerPaint.setAntiAlias(true);
		playerPaint.setStyle(Paint.Style.STROKE);
		playerPaint.setColor(Color.BLACK);

		//落下オブジェクト設定
		fallObjRadius = width / 30;
		fallObjPaint = new Paint();
		fallObjPaint.setAntiAlias(true);
		fallObjPaint.setStyle(Paint.Style.STROKE);
		fallObjPaint.setColor(Color.BLACK);

		//プレイヤー初期座標
		playerX = width / 2;
		playerY = height - (height / 4) - playerRadius;

		//落下オブジェクト初期座標
		fallObjX = new Random().nextInt(width - playerRadius * 2) + playerRadius;
		fallObjY = -fallObjRadius;
		objectArray = new ArrayList<FallObject>();
		objectArray.add(new FallObject(fallObjX, fallObjY, 0));
		
		//ビットマップサイズ変更
		bm = Bitmap.createScaledBitmap(bm, fallObjRadius * 2, fallObjRadius * 2, false);

		scoreView.setText(String.valueOf(score));

		final SurfaceHolder h = holder;
		final Handler handler = new Handler();
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while(thread != null) {
					Canvas canvas = h.lockCanvas();
					canvas.drawColor(Color.WHITE);

					//プレイヤー生成、座標更新
					if(movePlayer == 1 && playerX > playerRadius)
						playerX -= width / 100;
					if(movePlayer == 2 && playerX < width - playerRadius)
						playerX += width / 100;
					canvas.drawCircle(playerX, playerY, playerRadius, playerPaint);

					//オブジェクト初期座標生成
					if(objectDelay == OBJECT_DEF_DELAY) {
						objectArray.add(new FallObject(
								new Random().nextInt(width - playerRadius * 2) + playerRadius,
								-fallObjRadius, objectType()));
						objectDelay = 0;

					} else {
						objectDelay ++;
					}

					//オブジェクト生成、削除、座標更新
					for(int i=0; i<objectArray.size(); i++) {
						if(objectArray.get(i).getObjectY() < height + fallObjRadius) {
							if(isContact(objectArray.get(i).getObjectX(), objectArray.get(i).getObjectY())) {
								if(objectArray.get(i).getObjectType() == 0) {
									
									////////ゲームオーバー
									thread = null;
									canvas.drawBitmap(bm, objectArray.get(i).getObjectX(), 
											objectArray.get(i).getObjectY(), null);
																		
								} else {
									
									////////得点
									objectArray.remove(i);
									handler.post(new Runnable() {

										@Override
										public void run() {
											scoreView.setText(String.valueOf(++score));

										}
									});
								}
								

							} else {
								if(objectArray.get(i).getObjectType() == 0) {
									canvas.drawBitmap(bm, objectArray.get(i).getObjectX(), 
											objectArray.get(i).getObjectY(), null);

								} else {
									canvas.drawCircle(objectArray.get(i).getObjectX(),
											objectArray.get(i).getObjectY(), fallObjRadius, fallObjPaint);

								}
								objectArray.get(i).setObjectY(objectArray.get(i).getObjectY() + height / 100);
							}

						} else {
							
							///////画面外
							objectArray.remove(i);
						}
					}
					h.unlockCanvasAndPost(canvas);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}

			}
		});
		thread.start();

	}

	//衝突判定
	private boolean isContact(int objX, int objY) {
		int area = playerRadius + fallObjRadius;
		if(objX < playerX + area && objX > playerX - area
				&& objY < playerY  && objY > playerY - area) {
			return true;
		}
		return false;
	}

	//オブジェクトの種類を決める
	private int objectType() {
		if(BOMB_RATE >= Math.random()) {
			return 0;
		}
		return 1;
	}

	//タッチイベント処理
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			if((int)event.getX() < width / 2) {
				movePlayer = 1;
			} else {
				movePlayer = 2;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			movePlayer = -1;
			break;
		default:
			break;
		}

		return true;

	}

	class FallObject {
		private int objectX;
		private int objectY;
		private int objectType;

		public FallObject(int x, int y, int type) {
			objectX = x;
			objectY = y;
			objectType = type;
		}

		public void setObjectX(int x) {
			objectX = x;
		}

		public void setObjectY(int y) {
			objectY = y;
		}

		public void setType(int type) {
			objectType = type;
		}

		public int getObjectX() {
			return objectX;
		}

		public int getObjectY() {
			return objectY;
		}

		public int getObjectType() {
			return objectType;
		}
	}




}
