Creator	"yFiles"
Version	"2.7"
graph
[
	hierarchic	1
	label	""
	directed	1
	node
	[
		id	0
		label	"Node 1"
		graphics
		[
			x	67.220703125
			y	40.98076248168945
			w	30.0
			h	30.0
			type	"rectangle"
			fill	"#CCCCFF"
			outline	"#000000"
		]
		LabelGraphics
		[
			text	"Node 1"
			fontSize	12
			fontName	"Dialog"
			anchor	"c"
		]
	]
	node
	[
		id	1
		label	"node 2"
		graphics
		[
			x	22.220703125
			y	15.0
			w	30.0
			h	30.0
			type	"rectangle"
			fill	"#CCCCFF"
			outline	"#000000"
		]
		LabelGraphics
		[
			text	"node 2"
			fontSize	12
			fontName	"Dialog"
			anchor	"c"
		]
	]
	node
	[
		id	2
		label	"node 3"
		graphics
		[
			x	22.220703125
			y	66.9615249633789
			w	30.0
			h	30.0
			type	"rectangle"
			fill	"#CCCCFF"
			outline	"#000000"
		]
		LabelGraphics
		[
			text	"node 3"
			fontSize	12
			fontName	"Dialog"
			anchor	"c"
		]
	]
	edge
	[
		source	0
		target	1
		label	"Edge from node 1 to node 2"
		graphics
		[
			fill	"#000000"
		]
		LabelGraphics
		[
			text	"Edge from node 1 to node 2"
			fontSize	12
			fontName	"Dialog"
			model	"six_pos"
			position	"tail"
		]
	]
	edge
	[
		source	1
		target	2
		label	"Edge from node 2 to node 3"
		graphics
		[
			fill	"#000000"
		]
		LabelGraphics
		[
			text	"Edge from node 2 to node 3"
			fontSize	12
			fontName	"Dialog"
			model	"six_pos"
			position	"tail"
		]
	]
	edge
	[
		source	2
		target	0
		label	"Edge from node 3 to node 1"
		graphics
		[
			fill	"#000000"
		]
		LabelGraphics
		[
			text	"Edge from node 3 to node 1"
			fontSize	12
			fontName	"Dialog"
			model	"six_pos"
			position	"tail"
		]
	]
]
