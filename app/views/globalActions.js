/**
 * Autocomplete the name of the player
 */
var playerSuggestionsAction = #{jsAction @Application.searchPlayer(':query') /}
$(".ajax_players_list").autocomplete({
	source: function(request, response) {
		$.getJSON(playerSuggestionsAction({query:request.term}), function (data) {
			var suggestions = [];
			$.each(data, function(i, playerObject) {
				suggestions.push(playerObject.name)
			})
			response(suggestions)
		})
	},
	minLength: 2,
	select: function( event, ui ) {
		$(this).val(ui.item.value)
	}
});

/**
 * Autocomplete the name of the building
 */
var buildingSuggestionsAction = #{jsAction @Application.searchBuilding(':query') /}
$(".ajax_buildings_list").autocomplete({
	source: function(request, response) {
		$.getJSON(buildingSuggestionsAction({query:request.term}), function (data) {
			var suggestions = [];
			$.each(data, function(i, playerObject) {
				suggestions.push(playerObject.name)
			})
			response(suggestions)
		})
	},
	minLength: 2,
	select: function( event, ui ) {
		$(this).val(ui.item.value)
	}
});


/**
* Timers overlay save/restore position & size
*/
var setOverlayPositionAction = #{jsAction @Preferences.setOverlayPosition(':x',':y',':h',':w') /}
$("#timers-overlay").bind( "dragstop", function(event, ui) {
	var x = $(window).width() - $(this).offset().left - $(this).outerWidth(true)
	var y = $(window).height() - $(this).offset().top - $(this).outerHeight(true)
	var h = $(this).height()
	var w = $(this).width()
	$.get(setOverlayPositionAction({x:x,y:y,h:h,w:w}), function(data) {
		// no callback here
	})
});