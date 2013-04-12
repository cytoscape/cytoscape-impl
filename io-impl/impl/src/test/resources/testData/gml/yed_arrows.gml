Creator	"yFiles"
Version	"2.10"
graph
[
	hierarchic	1
	label	""
	directed	1
	node
	[
		id	0
		label	"n1"
		graphics
		[
			x	55.0
			y	53.0
			w	30.0
			h	30.0
			type	"ellipse"
			fill	"#FFCC00"
			outline	"#000000"
		]
		LabelGraphics
		[
			text	"n1"
			fontSize	12
			fontName	"Dialog"
			model	"null"
		]
	]
	node
	[
		id	1
		label	"n2"
		graphics
		[
			x	344.0
			y	53.0
			w	30.0
			h	30.0
			type	"ellipse"
			fill	"#FFCC00"
			outline	"#000000"
		]
		LabelGraphics
		[
			text	"n2"
			fontSize	12
			fontName	"Dialog"
			model	"null"
		]
	]
	node
	[
		id	2
		label	"n3"
		graphics
		[
			x	201.0
			y	214.0
			w	30.0
			h	30.0
			type	"ellipse"
			fill	"#FFCC00"
			outline	"#000000"
		]
		LabelGraphics
		[
			text	"n3"
			fontSize	12
			fontName	"Dialog"
			model	"null"
		]
	]
	node
	[
		id	3
		label	"n4"
		graphics
		[
			x	201.0
			y	332.0
			w	30.0
			h	30.0
			type	"ellipse"
			fill	"#FFCC00"
			outline	"#000000"
		]
		LabelGraphics
		[
			text	"n4"
			fontSize	12
			fontName	"Dialog"
			model	"null"
		]
	]
	edge
	[
		source	0
		target	1
		label	""
		graphics
		[
			fill	"#000000"
			targetArrow	"standard"
		]
		LabelGraphics
		[
		]
	]
	edge
	[
		source	1
		target	2
		label	""
		graphics
		[
			fill	"#000000"
			sourceArrow	"white_delta"
		]
		LabelGraphics
		[
		]
	]
	edge
	[
		source	2
		target	0
		label	""
		graphics
		[
			fill	"#000000"
			sourceArrow	"diamond"
			targetArrow	"circle"
		]
		LabelGraphics
		[
		]
	]
	edge
	[
		source	2
		target	3
		label	""
		graphics
		[
			fill	"#000000"
		]
		LabelGraphics
		[
		]
	]
]
