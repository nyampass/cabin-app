(function() {
  var sketch = new Processing.Sketch();
  // attach function (also, can be specified as the single parameter
  // in the Processing.Sketch object constructor)
  sketch.attachFunction = function(processing) {
    var radius = 50.0;
    var X, Y;
    var nX, nY;
    var delay = 16;

    processing.setup = function() {
      processing.size(200, 200);
      processing.strokeWeight(10);
      processing.frameRate(15);
      X = processing.width / 2.0;
      Y = processing.height / 2.0;
      nX = X;
      nY = Y;
    };

    processing.draw = function() {
      radius += sin(processing.frameCount / 4.0);

      X += (nX - X) / delay;
    }

    function texturedCube(tex) {
      processing.beginShape(processing.QUADS);
      processing.texture(tex);

      // Given one texture and six faces, we can easily set up the uv coordinates
      // such that four of the faces tile "perfectly" along either u or v, but the other
      // two faces cannot be so aligned.  This code tiles "along" u, "around" the X/Z faces
      // and fudges the Y faces - the Y faces are arbitrarily aligned such that a
      // rotation along the X axis will put the "top" of either texture at the "top"
      // of the screen, but is not otherwised aligned with the X/Z faces. (This
      // just affects what type of symmetry is required if you need seamless
      // tiling all the way around the cube)

      // +Z "front" face
      processing.vertex(-1, -1,  1, 0, 0);
      processing.vertex( 1, -1,  1, 1, 0);
      processing.vertex( 1,  1,  1, 1, 1);
      processing.vertex(-1,  1,  1, 0, 1);

      // -Z "back" face
      processing.vertex( 1, -1, -1, 0, 0);
      processing.vertex(-1, -1, -1, 1, 0);
      processing.vertex(-1,  1, -1, 1, 1);
      processing.vertex( 1,  1, -1, 0, 1);

      // +Y "bottom" face
      processing.vertex(-1,  1,  1, 0, 0);
      processing.vertex( 1,  1,  1, 1, 0);
      processing.vertex( 1,  1, -1, 1, 1);
      processing.vertex(-1,  1, -1, 0, 1);

      // -Y "top" face
      processing.vertex(-1, -1, -1, 0, 0);
      processing.vertex( 1, -1, -1, 1, 0);
      processing.vertex( 1, -1,  1, 1, 1);
      processing.vertex(-1, -1,  1, 0, 1);

      // +X "right" face
      processing.vertex( 1, -1,  1, 0, 0);
      processing.vertex( 1, -1, -1, 1, 0);
      processing.vertex( 1,  1, -1, 1, 1);
      processing.vertex( 1,  1,  1, 0, 1);

      // -X "left" face
      processing.vertex(-1, -1, -1, 0, 0);
      processing.vertex(-1, -1,  1, 1, 0);
      processing.vertex(-1,  1,  1, 1, 1);
      processing.vertex(-1,  1, -1, 0, 1);

      processing.endShape();
    }

    // mouse event
    processing.mouseDragged = function() {
      var rate = 0.01;
      rotx += (processing.pmouseY-processing.mouseY) * rate;
      roty += (processing.mouseX-processing.pmouseX) * rate;
    };
  };
 })();
