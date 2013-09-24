/**
 *
 * Template code for Cytoscape.js export function.
 *
 */
$(function () {
    // Basic settings for the Cytoscape window
    var options = {

        showOverlay: false,
        minZoom: 0.1,
        maxZoom: 20,

        style: cytoscape.stylesheet(
        ).selector('node').css({
            'content': 'data(name)',
            'font-family': 'helvetica neu',
            'font-size': 11,
            'text-valign': 'center',
            'color': 'green',
            'width': 'mapData(Degree, 1, 50, 30, 300)',
            'height': 'mapData(Degree, 1, 50, 30, 300)',
            'background-color': 'mapData(Degree, 1, 30, red, green)',
            'background-opacity': 0.6
        }).selector('node[Degree = 1]').css({ // Emulating discrete mapper.
            'background-color': '#eeaa33'
        }).selector('node[name = \'YNL216W\']').css({ // Emulating discrete mapper.
            'shape': 'rectangle'
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
            name: 'preset'
        },

        ready: function () {
            cy = this;
            cy.load(networkData.elements);
        }
    };

    var networkData = {};

    // Load JSON file
    $.getJSON('data/galFiltered.cyjs', function(network) {
        networkData = network;
        $('.network').cytoscape(options);
    });
});