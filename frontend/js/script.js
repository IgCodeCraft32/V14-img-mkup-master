let canvas, ctx;
var img;
var cPushArray = new Array();
var cStep = -1;
let isDrawing = false;
let lineColor = "red"; // was '#ACD3ED'
let thick = 5;
let fontSize = 25;

// https://www.w3schools.com/graphics/canvas_text.asp
// https://stackoverflow.com/questions/8429011/how-to-write-text-on-top-of-image-in-html5-canvas
// https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API/Tutorial/Drawing_text

function cPush() {
  cStep++;
  if (cStep < cPushArray.length) {
    cPushArray.length = cStep;
  }
  cPushArray.push(document.getElementById("drawContainer").toDataURL());
}

window.onload = function () {
  // nothing to do onload
  // in Production "drawContainer" is not available at this time, so do not set up yet
};
var coord = { x: 0, y: 0 };

function resize() {
  ctx.canvas.width = 1000;
  ctx.canvas.height = 1000;
}

function start(event, tool) {
  if (tool == "free")
    document
      .getElementById("drawContainer")
      .addEventListener("mousemove", drawFree);
  if (tool == "text") {
    return;
  }
  isDrawing = true;
  reposition(event);
}

function stop(event, tool, txt = "") {
  if (tool == "free") {
    document
      .getElementById("drawContainer")
      .removeEventListener("mousemove", drawFree);
  }
  if (tool == "text") {
    ns.drawText(event, txt);
  }
  if (isDrawing) {
    cPush();
  }
  isDrawing = false;
}

const vaading_vertical_layout_padding = 0; // 12 ?

function reposition(event) {
  let rect = canvas.getBoundingClientRect();
  coord.x = event.clientX - rect.left + vaading_vertical_layout_padding;
  coord.y = event.clientY - rect.top + vaading_vertical_layout_padding;
}

function drawFree(event) {
  console.log(
    "####### draw lineThickness=" + thick + " lineColor=" + lineColor
  );
  ctx.beginPath();
  ctx.lineWidth = thick;
  ctx.lineCap = "round";
  ctx.strokeStyle = lineColor;
  ctx.moveTo(coord.x, coord.y);
  reposition(event);
  ctx.lineTo(coord.x, coord.y);
  ctx.stroke();
}

window.ns = {
  undo: function () {
    if (cStep > 0) {
      cStep--;
      var canvasPic = new Image();
      canvasPic.src = cPushArray[cStep];
      canvasPic.onload = function () {
        ctx.drawImage(canvasPic, 0, 0);
      };
    }
  },

  redo: function () {
    if (cStep < cPushArray.length - 1) {
      cStep++;
      var canvasPic = new Image();
      canvasPic.src = cPushArray[cStep];
      canvasPic.onload = function () {
        ctx.drawImage(canvasPic, 0, 0);
      };
    }
  },

  getEditedJpg: function () {
    return canvas.toDataURL("image/jpeg");
  },

  removeEventListener: function () {
    var editedJpg = ns.getEditedJpg();
    var old_element = document.getElementById("drawContainer");
    var new_element = old_element.cloneNode(true);
    old_element.replaceWith(new_element);

    if (canvas != null) {
      // this is not the first image, so clear the canvas so we can start again ...
      canvas = null;
      ctx = null;
      img = null;
    }
    // Make sure we have canvas, ctx, & img ...
    canvas = document.getElementById("drawContainer");
    ctx = canvas.getContext("2d");
    window.addEventListener("resize", resize);
    img = new Image(); // force new image
    // we know everything is ready so load the image ...
    img.src = editedJpg;
    img.onload = function () {
      ctx.drawImage(img, 0, 0);
    };
  },

  setTool: function (tool, txt = "") {
    this.removeEventListener();
    document
      .getElementById("drawContainer")
      .addEventListener("mousedown", function (event) {
        start(event, tool);
      });
    document
      .getElementById("drawContainer")
      .addEventListener("mouseup", function (event) {
        stop(event, tool, txt);
      });
  },

  setImage: function (str, w, h) {
    if (canvas != null) {
      // this is not the first image, so clear the canvas so we can start again ...
      canvas = null;
      ctx = null;
      img = null;
    }
    // Make sure we have canvas, ctx, & img ...
    canvas = document.getElementById("drawContainer");
    ctx = canvas.getContext("2d");
    window.addEventListener("resize", resize);
    img = new Image(); // force new image
    // we know everything is ready so load the image ...
    img.src = str;
    ctx.canvas.width = w;
    ctx.canvas.height = h;
    img.onload = function () {
      ctx.drawImage(img, 0, 0);
      cPush();
    };
  },

  setCfg: function (drawcolor, thickness, fontSz) {
    // more cfg to come in but this is the start for the freeform stuff
    lineColor = drawcolor;
    thick = thickness;
    fontSize = fontSz;
  },

  drawText: function (event, txt) {
    console.log(
      "####### drawText" +
        " fontSize=" +
        fontSize +
        " txt=" +
        txt +
        " lineThickness=" +
        thick +
        "< lineColor=" +
        lineColor +
        "< event=" +
        event
    );
    isDrawing = true;
    canvas = document.getElementById("drawContainer");
    ctx = canvas.getContext("2d");
    ctx.font = fontSize + "px Arial";
    ctx.fillStyle = lineColor;
    let rect = canvas.getBoundingClientRect();

    coord.x = event.clientX - rect.left + vaading_vertical_layout_padding;
    coord.y = event.clientY - rect.top + vaading_vertical_layout_padding;
    ctx.fillText(txt, coord.x, coord.y);
  },
};
