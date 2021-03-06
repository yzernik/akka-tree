

    var events = [
        {"actorpath" : "akka://somesys/user/parent1", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent2/child2/abc/def/ghi", "event" : { "type" : "mailboxsizechanged", "size": 10 }},
        {"actorpath" : "akka://somesys/user/parent1/child2/abc/def/ghi", "event" : { "type" : "mailboxsizechanged", "size":5 }},
        {"actorpath" : "akka://somesys/user/parent1/child3/abc/def/ghi", "event" : { "type" : "mailboxsizechanged", "size": 3 }},
        {"actorpath" : "akka://somesys/user/parent2/child3/abc/def/ghi", "event" : { "type" : "terminated" } },
        {"actorpath" : "akka://somesys/user/parent1/child1/abc/def/ghi", "event" : { "type" : "terminated" } },
        {"actorpath" : "akka://somesys/user/parent1", "event" : { "type" : "terminated" } },
        {"actorpath" : "akka://somesys/user/parent1/child1/abc/def/ghi", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent1/child2/abc/def/ghi", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent1", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent2/child1/abc/def/ghi", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent1/child1/abc/def/ghi", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent1/child3/abc/def/ghi", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent1/child1/abc/def/ghi", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent2/child2/abc/def/ghi", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent2/child2/abc/def/ghi", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent2/child3/abc/def/ghi", "event" : { "type" : "started" } },
        {"actorpath" : "akka://somesys/user/parent2", "event" : { "type" : "started" } },
    ];


  var w = 1280,
    h = 800,
    node,
    link,
    root;

    var id = 1;

    function insert(path, parent, actorpath, level) {
      if (path.length == 0) { return; }
      else {
        var elem = path.shift();
        var node;
        if (parent.children) {
          node = parent.children.find(function(e) { return e.name == elem; });
        }
        if (!node) {
          node = {"name" : elem, "size": 1, "id": id++, "level" : level};
          if (!parent.children) {
            parent.children = [];
          }
          parent.children.push(node);
        }
        if (path.length == 0) {
          node.actorpath = actorpath;
        }
        insert(path, node, actorpath, level + 1);
      }
    }

    function remove(path, parent){

      var parent_ = parent;

      while (path.length > 1) {
        var elem = path.shift();
        if (parent_ && parent_.children) {
          parent_ = parent_.children.find(function(e) { return e.name == elem; });
        }
      }

      if (parent_ && parent_.children) {
        var elem = parent_.children.find(function(e) { return e.name == path[0]; });
        if (elem) {
            var index = parent_.children.indexOf(elem);
            if (index > -1) {
                parent_.children.splice(index, 1);
            }
        }
      }
    }

    function updateMsgSize(path, parent, size) {

      var parent_ = parent;

      while (path.length > 1) {
        var elem = path.shift();
        if (parent_ && parent_.children) {
          parent_ = parent_.children.find(function(e) { return e.name == elem; });
        }
      }
      if (parent_ && parent_.children) {
        var elem = parent_.children.find(function(e) { return e.name == path[0]; });
        if (elem) {
            elem.size = size;
        }
      }
    }


    function akkatree_onmessage(msg) {
      document.getElementById("log").innerHTML = JSON.stringify(msg);

      var path = msg.actorpath.replace(/akka:\/\/[^\/]+\/user\//,'').split("/");
      if (msg.event.type == "started") {
        insert(path, root, msg.actorpath, 0);
      } if (msg.event.type == "terminated") {
        remove(path, root);
      } if (msg.event.type == "mailboxsizechanged") {
         insert(path, root, msg.actorpath, 0);
         path = msg.actorpath.replace(/akka:\/\/[^\/]+\/user\//,'').split("/");
         updateMsgSize(path, root, msg.event.size);
      }
      update();
    }

    function eventsource() {
      var evt = events.pop();
      akkatree_onmessage(evt);
      if (events.length != 0) {
        window.setTimeout(eventsource, 1000);
      }
    }
//    window.setTimeout(eventsource, 0);
    root = {"name": "user", "size": 0, "id" : 0, "children" : [], "actorpath" : "ActorSystem" };
    root.fixed = true;
    root.x = w / 2;
    root.y = h / 2 - 80;



var force = d3.layout.force()
    .on("tick", tick)
    .charge(function(d) { return -500; })
//    .charge(function(d) { return d._children ? -d.size / 100 : -30; })
    .linkDistance(function(d) { return 50; })
    .size([w, h - 160]);

var vis = d3.select("#canvas").append("svg:svg")
    .attr("width", w)
    .attr("height", h);

function update() {
  var nodes = flatten(root),
      links = d3.layout.tree().links(nodes);

  // Restart the force layout.
  force
      .nodes(nodes)
      .links(links)
      .start();

  // Update the links…
  link = vis.selectAll("line.link")
      .data(links, function(d) { return d.target.id; });

  // Enter any new links.
  link.enter().insert("svg:line", ".node")
      .attr("class", "link")
      .attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  // Exit any old links.
  link.exit().remove();

  // Update the nodes…
  node = vis.selectAll("circle.node")
      .data(nodes, function(d) { return d.id; })
      .style("fill", color);

  //node.transition()
  //    .attr("r", function(d) { return d.children ? 4.5 : Math.sqrt(d.size) / 10; });

  // Enter any new nodes.
  node.enter().append("svg:circle")
      .attr("class", "node")
      .attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; })
      .attr("r", function(d) { return Math.sqrt((d.size + 1) * 100); })
      .style("fill", color)
      .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
      .on("click", click)
      .call(force.drag);

  // Exit any old nodes.
  node.exit().remove();

  $('svg circle').tipsy({
    gravity: 'w',
    html: true,
    title: function() {
        var d = this.__data__;
        console.log(d);
        return 'Path: ' + d.actorpath + '';
    }
  });

}

function tick() {
  link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  node.attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; });
}

function color(d) {
  var colors = ["#1d4d70", "#3182bd", "#c6dbef", "#ffffff"];
  //console.log("level " + d.level);
  return d.name == "user" ? "#ff0000" : colors[d.level % colors.length];
}

// Toggle children on click.
function click(d) {
  //document.getElementById("actorname").innerHTML = d.actorpath;
}

// Returns a list of all nodes under the root.
function flatten(root) {
  var nodes = [], i = 0;

  function recurse(node) {
    if (node.children) node.children.reduce(function(p, v) { return p + recurse(v); }, 0);
    nodes.push(node);
    return node.size;
  }

  recurse(root);
  return nodes;
}
