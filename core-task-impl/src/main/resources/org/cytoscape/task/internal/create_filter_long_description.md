Create Filter Documentation
===========================

The 'filter create' command creates a filter from JSON formatted string.

One way to create the JSON for a filter is to open Cytoscape, go to the 
**Select** tab, manually create a filter, click the menu button, then
select **Export Filters...**. This will create a JSON file containing
the same JSON format that is accepted by the 'filter create' command.

JSON can be embedded in the command using a single-quoted string. Example:

```
filter create name="myfilter" json='{ "id" : "ColumnFilter", "parameters" : { "criterion" : "1", "columnName" : "name", "predicate" : "CONTAINS"} }'
```

JSON Format
-----------

Filter JSON follows this structure...

```
{
  "id" : "filter-id",
  "parameters" : {
    "parameter1" : "value1",
    "parameter2" : "value2",
    ...
  },
  "transformers" : [ 
    ...
   ]
}
```

Each Filter has an "id" followed by a "parameters" sub-object. Some filters
can have child filters, these have a "transformers" field which contains an
array of filters.


Column Filter
-------------

Selects nodes/edges based on the value in a table column.

Example:

```
{
  "id" : "ColumnFilter",
  "parameters" : {
    "predicate" : "BETWEEN",
    "criterion" : [ 1, 2 ],
    "caseSensitive" : false,
    "type" : "nodes",
    "anyMatch" : true,
    "columnName" : "name"
  }
}
```

**predicate**

  For boolean columns: IS, IS\_NOT
  
  For string columns: IS, IS\_NOT, CONTAINS, DOES\_NOT\_CONTAIN, REGEX
  
  For numeric columns: IS, IS\_NOT, GREATER\_THAN, GREATER\_THAN\_OR\_EQUAL, LESS\_THAN, LESS\_THAN\_OR\_EQUAL, BETWEEN, IS\_NOT\_BETWEEN
  
**criterion**

  For boolean columns: true, false
  
  For string columns: a string value, eg "hello". If the predicate is REGEX then this can be a regular expression as accepted by the Java Pattern class (https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html)
  
  For numeric columns: If the predicate is BETWEEN or IS\_NOT\_BETWEEN then this is a two-element array of numbers, example: [1, 5], otherwise a single number example: 1
  
**columnName**

  The name of the column.
  
**caseSensitive** (optional, default false)

  true if string matching should be case sensitive, false otherwise

**anyMatch** (optional, default true)

  Only applies to List columns. If *true* then at least one element in the list must pass the filter, if *false* then all the elements in the list must pass the filter.
  
**type** (optional, default "nodes")

  Values: "nodes", "edges", "nodes+edges" 

  If "nodes" the filter is applied only to nodes, if "edges" the filter is applied only to edges, if "nodes+edges" then the filter is applied to both.
  Note that for "nodes+edges" to work as expected the node and edge tables should have a column with the same name.
  
 
Degree Filter
-------------

Selects nodes based on the in/out degree.

Example:

```
{
  "id" : "DegreeFilter",
  "parameters" : {
    "predicate" : "BETWEEN",
    "criterion" : [ 0, 1 ],
    "edgeType" : "ANY"
  }
}
```

**predecate**

BETWEEN, IS\_NOT\_BETWEEN

**criterion**

A two-element array of numbers, example: [1, 5]

**edgeType**

ANY, INCOMING, OUTGOING


Topology Filter
---------------

Selects nodes based on how many neighbors they have. Neighbor nodes must also pass the child filters.

Example:

```
{
  "id" : "TopologyFilter",
  "parameters" : {
    "predicate" : "GREATER_THAN_OR_EQUAL",
    "distance" : 3,
    "threshold" : 2,
    "type" : "ALL"
  },
  "transformers" : [ {
    "id" : "ColumnFilter",
    "parameters" : {
      "predicate" : "CONTAINS",
      "criterion" : "1",
      "columnName" : "name"
    }
  } ]
}
```

**predicate**

GREATER\_THAN\_OR\_EQUAL, LESS\_THAN

**distance**

The distance from the node other nodes can be to be considered neighbors.

**threshold**

If predicate is LESS\_THAN then there must be less than this many neighbors for the node to pass the filer.

**type**

ANY, ALL

If ANY then at least one of the child filters must pass for the node to pass. If ALL then all of the child filters must pass.

**transformers**

Array of child filters. These are applied to each neighbor node to determine how many neighbors to consider.


Composite Filter
----------------

A composite filter contains a list of child filters.

Example:

```
{ 
  "id" : "CompositeFilter",
  "parameters" : {
     "type" : "ALL"
   },
   "transformers" : [ ]
}
```

**type**

ANY, ALL

If ANY then at least one of the child filters must pass for the node to pass. If ALL then all of the child filters must pass.

**transformers**

Array of child filters.

