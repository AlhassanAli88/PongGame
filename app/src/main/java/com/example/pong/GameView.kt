package com.example.pong

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.fragment.app.commit

class GameView(context: Context):SurfaceView(context), SurfaceHolder.Callback, Runnable{
    var thread: Thread? = null
    var running = false
    lateinit var canvas: Canvas
    var limit = Rect()
    var objects: ArrayList<Object> = ArrayList()
    var mHolder : SurfaceHolder? = holder
    var objectsCreated: Int = 0
    var score: Int = 0
    var bestScore: Int = 0
    var gameActivity = context as? GameActivity
    var stop = false
    var touchX: Float? = null
    var touchY: Float? = null
    val mode = 1

    private var background1: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.stars)
    var mutablebackground = background1.copy(Bitmap.Config.ARGB_8888, true)



    init {
        var playerList = DataManager.playerListM1
        if (!playerList.isEmpty()) {
            for (i in 0..playerList.size - 1) {
                if (playerList[i].score > bestScore)
                    bestScore = playerList[i].score
            }
        }
    }



    init {

        if (context is GameActivity) {
            gameActivity = context
        }
        if (mHolder != null){
            mHolder?.addCallback(this)
        }
        objects.add(PongBall(this, "PongBall", 300f, 100f, 12f,
            14f,50f,BitmapFactory.decodeResource(context.resources, R.drawable.asteroid)))



        objects.add(Paddle(this, "Paddle", 300f, 2200f, 0f,
            0f,300f,50f,BitmapFactory.decodeResource(context.resources, R.drawable.paddel)))
    }






    fun start(){
        running = true
        thread = Thread(this)
        thread?.start()
    }

    fun stop(){
        running = false
        thread?.join()
    }

   fun update(){
        objects.forEach{
            it.update()

        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        touchX = event?.x
        touchY = event?.y

        return true
    }


    fun draw() {
        // Check if holder is null
        val currentHolder = holder ?: return

        // Lock the canvas
        canvas = currentHolder.lockCanvas() ?: return

        try {
            // Draw on the canvas
            canvas.drawBitmap(mutablebackground, matrix, null)
            objects.forEach {
                it.draw(canvas)
            }

            // Draw score
            val textPaint = Paint().apply {
                textSize = 50f
                color = Color.YELLOW
            }
            canvas.drawText("Score: $score", 100f, 100f, textPaint)
            canvas.drawText("Best Ever: $bestScore",  700f, 100f, textPaint)

        } finally {
            // Unlock the canvas in a final block to ensure it always happens
            currentHolder.unlockCanvasAndPost(canvas)
        }
    }



    fun saveScore(){
        gameActivity!!.supportFragmentManager.commit {

            val saveFragment = SaveFragment()
            var bundle = Bundle()
            bundle.putInt("Score", score)
            bundle.putInt("mode", mode)
            saveFragment.arguments = bundle
            replace(R.id.frame_play, saveFragment)
        }

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        limit = Rect(0, 0, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stop()
    }

    override fun run() {
        while (running){
            if(!stop)
                update()
            draw()
        }
    }

}