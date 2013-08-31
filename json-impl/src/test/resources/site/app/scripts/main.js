/**
 *
 * Template code for Cytoscpae.js export function.
 *
 */
$(loadCy = function () {


    // Basic settings for the Cytoscape window
    var options = {

        showOverlay: false,
        minZoom: 0.1,
        maxZoom: 20,

        style: cytoscape.stylesheet().selector('node').css({
            'content': 'data(name)',
            'font-family': 'helvetica neu',
            'font-size': 11,
            'text-valign': 'center',
            'color': 'white',
            'width': 40,
            'height': 40,
            'background-color': 'white',
            'background-opacity': 0.6
        }).selector(':selected').css({
                'background-color': 'red',
                'line-color': 'red',
                'color': 'white'
            }).selector('edge').css({
                'width': 4,
                'target-arrow-shape': 'triangle',
                'line-color': 'green'
            }),

        layout: {
            name: 'circle'

        },

        ready: function () {
            cy = this;
            cy.load(networkData.elements);
        }
    };


    var networkData = {};

    // Load JSON file
    $.getJSON('data/cyjs.json', function(network) {
        console.log(network);
        networkData = network;
        $('.network').cytoscape(options);
    });

}); 