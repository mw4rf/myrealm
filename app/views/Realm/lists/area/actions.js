/* Areas */
$('.realmAreaCell').live('click', function() {
	var area = $(this).attr('area')
	var playerName = $(this).attr('player')
	loading(true)
	$.post(loadAreaAction({playerName: playerName}), {area:area}, function(data) {
		$('#buildings_area_group').html($(data).fadeIn())
		loading(false)
	})
})

$('.realmAreaTextCell').live('click', function() {
	$('.realmAreaTextCell').removeClass('realmAreaTextCellSelected')
	$(this).addClass('realmAreaTextCellSelected')
})

$('.realmAreaImageCell').live('click', function() {
	$('.realmAreaImageCell').removeClass('realmAreaImageCellSelected')
	$(this).addClass('realmAreaImageCellSelected')
})