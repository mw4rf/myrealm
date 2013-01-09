
/**
 * Loads the chart for the given resource
 */
var loadStatsForGoodAction = #{jsAction @Realm.loadStatsForGood(':playerName') /}
$('.statslink').live('click', function() {
	if($(this).hasClass("good")) {
		var goodName = $(this).attr("good")
		var playerName = $(this).attr("player")
		$.post(loadStatsForGoodAction({playerName: playerName}), {goodName:goodName}, function(data) {
			$("#productionrelative").html(data)
		})
	}
})