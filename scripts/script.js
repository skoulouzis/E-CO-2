var w = 500,
	h = 500;

var colorscale = d3.scale.category10();



// var d = [
// 		  [
// 			{axis:"dsrm04",value:0.59},{axis:"dseng03",value:0.56},{axis:"dsdk04",value:0.42},
// 			{axis:"dsda03",value:0.34},
// 			{axis:"dsda05",value:0.48},
// 			{axis:"dsdk01",value:0.14},
// 			{axis:"dsda06",value:0.11},
// 			{axis:"dseng02",value:0.05},
// 			{axis:"dsrm05",value:0.07},
// 			{axis:"dsdk05",value:0.12},
// 			{axis:"dsda04",value:0.27},
// 			{axis:"dsdm03",value:0.03},
// 			{axis:"dsdm05",value:0.12},
// 			{axis:"dsdm01",value:0.4},
// 			{axis:"dsdm02",value:0.03},
// 			{axis:"dsrm03",value:0.22},
// 			{axis:"dseng01",value:0.03},
// 			{axis:"dsdk02",value:0.03},
// 			{axis:"dsrm06",value:0.07},
// 			{axis:"dsdm06",value:0.18},
// 			{axis:"dsdk06",value:0.07},
// 			{axis:"dseng06",value:0.08}
// 		  ],[
// 			{axis:"dsrm04",value:0.48},
// 			{axis:"dseng03",value:0.41},
// 			{axis:"dsdk04",value:0.27},
// 			{axis:"dsda03",value:0.28},
// 			{axis:"dsda05",value:0.46},
// 			{axis:"dsdk01",value:0.29},
// 			{axis:"dsda06",value:0.11},
// 			{axis:"dseng02",value:0.14},
// 			{axis:"dsrm05",value:0.05},
// 			{axis:"dsdk05",value:0.19},
// 			{axis:"dsda04",value:0.14},
// 			{axis:"dsdm03",value:0.06},
// 			{axis:"dsdm05",value:0.24},
// 			{axis:"dsdm01",value:0.17},
// 			{axis:"dsdm02",value:0.15},
// 			{axis:"dsrm03",value:0.12},
// 			{axis:"dseng01",value:0.1},
// 			{axis:"dsdk02",value:0.14},
// 			{axis:"dsrm06",value:0.06},
// 			{axis:"dsdm06",value:0.16},
// 			{axis:"dsdk06",value:0.07},
// 			{axis:"dseng06",value:0.17}
// 		  ]
// 		  ,[
// 			{axis:"dsrm04",value:0.48},
// 			{axis:"dseng03",value:0.41},
// 			{axis:"dsdk04",value:0.27},
// 			{axis:"dsda03",value:0.28},
// 			{axis:"dsda05",value:0.46},
// 			{axis:"dsdk01",value:0.29},
// 			{axis:"dsda06",value:0.11},
// 			{axis:"dseng02",value:0.14},
// 			{axis:"dsrm05",value:0.05},
// 			{axis:"dsdk05",value:0.19},
// 			{axis:"dsda04",value:0.14},
// 			{axis:"dsdm03",value:0.06},
// 			{axis:"dsdm05",value:0.24},
// 			{axis:"dsdm01",value:0.17},
// 			{axis:"dsdm02",value:0.15},
// 			{axis:"dsrm03",value:0.12},
// 			{axis:"dseng01",value:0.1},
// 			{axis:"dsdk02",value:0.14},
// 			{axis:"dsrm06",value:0.06},
// 			{axis:"dsdm06",value:0.16},
// 			{axis:"dsdk06",value:0.07},
// 			{axis:"dseng06",value:0.17}
// 		  ]
// 		];
// 		

var LegendOptions = [];
var d = [];//[ [ {axis:"dsrm04",value:0.59} ] ,[{axis:"dsrm04",value:0.48} ]];

d3.csv("course2.csv", function(data) {
//    
    
    data.forEach(function(line) {
    var array = [];  
    LegendOptions.push(line.field);
   
      for (var key in line) {
	  if (Object.prototype.hasOwnProperty.call(line, key)) {
	    if(key != "field"){
	      var val = line[key];
	      var obj = {axis: key,value:val}
	      array.push(obj);
	      d.push(array);
	    }
	  }
      } 
  });
   
    

    
//Options for the Radar chart, other than default
var mycfg = {
  w: w,
  h: h,
  maxValue: 0.8,
  levels: 5,
  opacityArea: 0
}

var margin = {top: 100, right: 100, bottom: 100, left: 100},
	width = Math.min(700, window.innerWidth - 10) - margin.left - margin.right,
	height = Math.min(width, window.innerHeight - margin.top - margin.bottom - 20);
				
var color = d3.scale.ordinal()
	.range(["#EDC951","#CC333F","#00A0B0"]);
var radarChartOptions = {
  w: w,
  h: h,
  margin: margin,
  maxValue: 0.6,
  levels: 5,
  roundStrokes: true,
  opacityArea: 0.1,
  color: color
};
			



//Call function to draw the Radar chart
//Will expect that data is in %'s
RadarChart.draw("#chart", d, radarChartOptions);

////////////////////////////////////////////
/////////// Initiate legend ////////////////
////////////////////////////////////////////

var svg = d3.select('#body')
	.selectAll('svg')
	.append('svg')
	.attr("width", w+300)
	.attr("height", h)

//Create the title for the legend
// var text = svg.append("text")
// 	.attr("class", "title")
// 	.attr('transform', 'translate(90,0)') 
// 	.attr("x", w - 70)
// 	.attr("y", 10)
// 	.attr("font-size", "12px")
// 	.attr("fill", "#404040")
// 	.text("What % of owners use a specific service in a week");
		
//Initiate Legend	
var legend = svg.append("g")
	.attr("class", "legend")
	.attr("height", 100)
	.attr("width", 200)
	.attr('transform', 'translate(90,20)') 
	;
	//Create colour squares
	legend.selectAll('rect')
	  .data(LegendOptions)
	  .enter()
	  .append("rect")
	  .attr("x", w - 65)
	  .attr("y", function(d, i){ return i * 20;})
	  .attr("width", 10)
	  .attr("height", 10)
	  .style("fill", function(d, i){ return colorscale(i);})
	  ;
	//Create text next to squares
	legend.selectAll('text')
	  .data(LegendOptions)
	  .enter()
	  .append("text")
	  .attr("x", w - 52)
	  .attr("y", function(d, i){ return i * 20 + 9;})
	  .attr("font-size", "11px")
	  .attr("fill", "#737373")
	  .text(function(d) { return d; })
	  ;	
	  
});


	  
	  



