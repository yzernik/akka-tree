

    var events = [
        {"actorPath" : "akka://somesys/user/parent2/child3", "event" : { "type" : "terminated" } },
        {"actorPath" : "akka://somesys/user/parent1/child1", "event" : { "type" : "terminated" } },
        {"actorPath" : "akka://somesys/user/parent1", "event" : { "type" : "terminated" } },
        {"actorPath" : "akka://somesys/user/parent1/child1", "event" : { "type" : "started" } },
        {"actorPath" : "akka://somesys/user/parent1/child2", "event" : { "type" : "started" } },
        {"actorPath" : "akka://somesys/user/parent1", "event" : { "type" : "started" } },
        {"actorPath" : "akka://somesys/user/parent2/child1", "event" : { "type" : "started" } },
        {"actorPath" : "akka://somesys/user/parent1/child1", "event" : { "type" : "started" } },
        {"actorPath" : "akka://somesys/user/parent1/child3", "event" : { "type" : "started" } },
        {"actorPath" : "akka://somesys/user/parent1/child1", "event" : { "type" : "started" } },
        {"actorPath" : "akka://somesys/user/parent2/child2", "event" : { "type" : "started" } },
        {"actorPath" : "akka://somesys/user/parent2/child3", "event" : { "type" : "started" } }
    ];


  var w = 1280,
    h = 800,
    node,
    link,
    root;

    var id = 1;

    function insert(path, parent) {
      if (path.length == 0) { return; }
      else {
        var elem = path.shift();
        var node;
        if (parent.children) {
          node = parent.children.find(function(e) { return e.name == elem; });
        }
        if (!node) {
          node = {"name" : elem, "size": 5000, "id": id++};
          if (!parent.children) {
            parent.children = [];
          }
          parent.children.push(node);
        }
        insert(path, node);
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

    function akkatree_onmessage(msg) {


      var path = msg.actorpath.replace(/akka:\/\/[^\/]+\/user\//,'').split("/");
      if (msg.event.type == "started") {
        insert(path, root);
      } if (msg.event.type == "terminated") {
        remove(path, root);
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
    root = {"name": "user", "size": 1, "id" : 0, "children" : [] };
    root.fixed = true;
    root.x = w / 2;
    root.y = h / 2 - 80;



var force = d3.layout.force()
    .on("tick", tick)
    .charge(function(d) { return d._children ? -d.size / 100 : -30; })
//    .charge(function(d) { return d._children ? -d.size / 100 : -30; })
    .linkDistance(function(d) { return d.target._children ? 150 : 100; })
    .size([w, h - 160]);

var vis = d3.select("body").append("svg:svg")
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

  node.transition()
      .attr("r", function(d) { return d.children ? 4.5 : Math.sqrt(d.size) / 10; });

  // Enter any new nodes.
  node.enter().append("svg:circle")
      .attr("class", "node")
      .attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; })
      .attr("r", function(d) { return d.children ? 4.5 : Math.sqrt(d.size) / 10; })
      .style("fill", color)
      .on("click", click)
      .call(force.drag);

// nodes.forEach(function(d) { d.x = d.depth * 10; });

  // Exit any old nodes.
  node.exit().remove();
}

function tick() {
  link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  node.attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; });
}

// Color leaf nodes orange, and packages white or blue.
function color(d) {
  return d._children ? "#3182bd" : d.children ? "#c6dbef" : "#fd8d3c";
}

// Toggle children on click.
function click(d) {
  if (d.children) {
    d._children = d.children;
    d.children = null;
  } else {
    d.children = d._children;
    d._children = null;
  }
  update();
}

// Returns a list of all nodes under the root.
function flatten(root) {
  var nodes = [], i = 0;

  function recurse(node) {
    if (node.children) node.size = node.children.reduce(function(p, v) { return p + recurse(v); }, 0);
    nodes.push(node);
    return node.size;
  }

  root.size = recurse(root);
  return nodes;
}
