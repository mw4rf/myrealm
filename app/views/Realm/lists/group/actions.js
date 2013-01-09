/** Groups **/
$('.realmGroupCell').live('click', function() {
	var groupId = $(this).attr('groupId')
	var playerName = $(this).attr('player')
	loading(true)
	$.post(loadGroupAction({playerName: playerName}), {groupId:groupId, sorting:null, order:null}, function(data) {
		$('#buildings_group_group').html($(data).fadeIn())
		loading(false)
		// Scroll to div
		$('html,body').animate({scrollTop: $("#buildings_group_group").offset().top},'slow')
	})
})

var loadRatiosForGroupAction = #{jsAction @Realm.loadRatiosForGroup(':playerName', ':groupId') /}