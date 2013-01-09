/**
 * List of buildings by TYPE
 */
var loadBuildingsAction = #{jsAction @Realm.loadBuildings(':playerName') /}
$(".loadBuildingsLink").live("click", function() {
	var playerName = $(this).attr("player")
	var buildingName = $(this).attr("building")
	loading(true)
	$.post(loadBuildingsAction({playerName: playerName}), {buildingName: buildingName}, function(data) {
		$("#buildings_group").html($(data).fadeIn())
		loading(false)
	})
	return false
})

$(".loadBuildingsField").live("keypress", function(key) {
	if(key.which == 13) { // 13 = ENTER/RETURN key
		var playerName = $(this).attr("player")
		var buildingName = $(this).val()
		loading(true)
		$.post(loadBuildingsAction({playerName: playerName}), {buildingName: buildingName}, function(data) {
			$("#buildings_group").html($(data).fadeIn())
			loading(false)
		})
		return false
	}
})

/**
 * List of buildings by AREA/GROUP/STATUS
 */
var loadAreaAction = #{jsAction @Realm.loadArea(':playerName') /}
var loadGroupAction = #{jsAction @Realm.loadGroup(':playerName') /}
var loadStatusAction = #{jsAction @Realm.loadStatus(':playerName') /}

/**
 * Sort the list of buildings. This method is used by the list by TYPE, GROUP, and AREA.
 */
$(".sortBuildings").live("click", function() {
	var sorting = $(this).attr("sorting")
	var order = $("input[name='sort-order']:checked").val()
	var playerName = $(this).attr("player")
	var buildingName = $(this).attr("building")
	var area = $(this).attr("area")
	var groupId = $(this).attr("groupId")
	var status = $(this).attr("status") // boosted, stopped, simulated
	loading(true)
	// Buildings by type
	if($(this).parents("#buildings_list").length > 0) {
		$.post(loadBuildingsAction({playerName: playerName}), {buildingName: buildingName, sorting: sorting, order: order}, function(data) {
			$("#buildings_group").html($(data).fadeIn())
			loading(false)
		})
	}
	// Buildings by area
	else if($(this).parents("#buildings_list_area").length > 0) {
		$.post(loadAreaAction({playerName: playerName}), {area: area, sorting: sorting, order: order}, function(data) {
			$('#buildings_area_group').html($(data).fadeIn())
			loading(false)
		})
	}
	// Buildings by group
	else if($(this).parents("#buildings_list_group").length > 0) {
		$.post(loadGroupAction({playerName: playerName}), {groupId: groupId, sorting: sorting, order: order}, function(data) {
			$('#buildings_group_group').html($(data).fadeIn())
			loading(false)
		})
	}
	// Buildings by status
	else if($(this).parents("#buildings-list-by-status").length > 0) {
		$.post(loadStatusAction({playerName: playerName}), {status: status, sorting: sorting, order: order}, function(data) {
			$('#buildings-list-by-status-ajax').html($(data).fadeIn())
			loading(false)
		})
	}
	// do not scroll up
	return false
})
