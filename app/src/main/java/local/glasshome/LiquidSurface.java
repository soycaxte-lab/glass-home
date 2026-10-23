package local.glasshome;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Lightweight glass: translucent tint, edge reflections and a clipped specular wash. No live blur. */
final class LiquidSurface extends Drawable {
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final RectF rect=new RectF(),inner=new RectF();
 private final Path clip=new Path();
 private final float density,radius;
 private final boolean focused;
 private final int tint;
 private Shader fill,edge,sheen;
 LiquidSurface(float density,float radius,boolean focused,int tint){this.density=density;this.radius=radius*density;this.focused=focused;this.tint=tint;}
 @Override protected void onBoundsChange(Rect bounds){
  rect.set(bounds.left+density,bounds.top+density,bounds.right-density,bounds.bottom-density);
  clip.reset();clip.addRoundRect(rect,radius,radius,Path.Direction.CW);
  int base=focused?0x283f587b:tint;
  fill=new LinearGradient(0,rect.top,rect.width()*.65f,rect.bottom,new int[]{focused?0x2de5f3ff:0x274e5973,base,focused?0x243c5277:0x18324062},new float[]{0,.45f,1},Shader.TileMode.CLAMP);
  edge=new LinearGradient(rect.left,rect.top,rect.right,rect.bottom,new int[]{focused?0xcceef7ff:0x72e6edff,0x14ffffff,focused?0x80aecfff:0x386f8ab7,focused?0xb3ffffff:0x60c6d0ec},new float[]{0,.36f,.72f,1},Shader.TileMode.CLAMP);
  sheen=new RadialGradient(rect.left+rect.width()*.17f,rect.top-rect.height()*.2f,Math.max(1,rect.width()*.75f),focused?0x30ffffff:0x20ffffff,Color.TRANSPARENT,Shader.TileMode.CLAMP);
 }
 @Override public void draw(Canvas canvas){
  paint.setAlpha(255);paint.setStyle(Paint.Style.FILL);paint.setShader(fill);canvas.drawRoundRect(rect,radius,radius,paint);
  int save=canvas.save();canvas.clipPath(clip);paint.setShader(sheen);canvas.drawRect(rect,paint);canvas.restoreToCount(save);
  paint.setShader(edge);paint.setStrokeWidth(density*(focused?1.15f:.8f));paint.setStyle(Paint.Style.STROKE);canvas.drawRoundRect(rect,radius,radius,paint);
  inner.set(rect);inner.inset(density*1.5f,density*1.5f);paint.setShader(null);paint.setColor(focused?0x24ffffff:0x0cffffff);paint.setStrokeWidth(density*.6f);canvas.drawRoundRect(inner,Math.max(0,radius-density),Math.max(0,radius-density),paint);
  paint.setStyle(Paint.Style.FILL);
 }
 @Override public void getOutline(Outline outline){outline.setRoundRect(getBounds(),radius);}
 @Override public void setAlpha(int alpha){}
 @Override public void setColorFilter(ColorFilter filter){}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}

