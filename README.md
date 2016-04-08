# ImpressionistPainter

My ImpressionistPainter

I edited three of the provided Java files, including ImpressionistView.java, MainActivity.java, and BrushType.java, along with a couple layout files: activity_main.xml and popup_menu.xml.

Inside the app, you can donwload some of the images that were provided in the original code, along with a couple that I added, to the device's gallery. You can also load an image from your gallery to the left side of the screen. Then you can choose from one of three brushes: circle, square, and triangle. The circle and square brushes are similar, in that each one simply takes the user's finger location, finds the corresponding pixel on the picture on the left, and draws a shape using the color of that pixel. The difference, of course, is that the circle brush draws a circle, and the square brush draws a square.

The triangle brush is a bit different because the size of the triangle changes with the velocity of the user's finger. Faster movements result in larger triangles. On the emulator, this does not work super well because it's a bit laggy, but I have not had trouble on my actual device. When moving slowly, more detail can be drawn because the figures get very small. 

My bonus feature is color inversion. If the user clicks the button for color inversion, the color drawn will be the inverse of the color it gets from the left picture. I got code to do this from http://stackoverflow.com/questions/4672271/reverse-opposing-colors in a comment by Simon Heinen.

The user can also save a drawing to the gallery when he or she is finished with it. I got the code to do this from http://stackoverflow.com/questions/8560501/android-save-image-into-gallery in a comment by sfratini. Originally, I was having trouble saving photos on the emulator because the proper directory did not exist. I had to add the piece of code from http://stackoverflow.com/questions/23059580/mediastore-images-media-insertimage-is-returning-null-when-trying-to-save-the-im in a comment by Dia Kharrat, which checks to see if the proper directory exists before trying to save to it.
