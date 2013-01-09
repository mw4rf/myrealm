

function realmToggleSections(bool) {

	// unfold
	if(bool) {
		$('#productionratios').slideDown()
		$('#suggestions').slideDown()
		$('#itemsratios').slideDown()
		$('#calculations').slideDown()
		$('#simulations').slideDown()
		$('#boostsbuildings').slideDown()
		$('#disabledbuildings').slideDown()
		$('#buildings_list').slideDown()
		$('#buildings_list_area').slideDown()
		$('#buildings_list_group').slideDown()
	} else {
		$('#productionratios').slideUp()
		$('#suggestions').slideUp()
		$('#itemsratios').slideUp()
		$('#calculations').slideUp()
		$('#simulations').slideUp()
		$('#boostsbuildings').slideUp()
		$('#disabledbuildings').slideUp()
		$('#buildings_list').slideUp()
		$('#buildings_list_area').slideUp()
		$('#buildings_list_group').slideUp()
	}

	return false;
}

/** Ajax load ratios */
function info_reload() {
	var playerName = $('#productionratios').attr("player")
	var page = $('#productionratios').attr('page')
	if(page == "group") {
		var groupId = $('#productionratios').attr('groupId')
		$.get(loadRatiosForGroupAction({playerName: playerName, groupId: groupId}), function(data) {
			$('#productionratios').html(data)
		})
	} else {
		$.get(loadRatiosAction({playerName: playerName}), function(data) {
			$('#productionratios').html(data)
		})
	}
}

/** Ajax load suggestions */
function makeSuggestions(playerName) {
	$('#suggestions_button').hide()
	$('#suggestions_result').hide()
	$('#suggestions_container .spinner').show()
	$.get(makeSuggestionsAction({playerName: playerName}), function(data) {
		  $('#suggestions_result').html(data)
		  $('#suggestions_container .spinner').hide()
		  $('#suggestions_result').show()
	});
}

function togglesuggestions(playerName) {
	var ct = $('#suggestions')
	if(ct.css('display') == 'none') {
		toggleshow(ct, false)
		makeSuggestions(playerName)
	}
	else
		toggleshow(ct)
}














