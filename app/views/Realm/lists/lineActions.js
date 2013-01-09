/**
 * AJAX update actions
 */
var loadRatiosForGroupAction = #{jsAction @Realm.loadRatiosForGroup(':playerName', ':groupId') /}
function loadRatiosForGroup(playerName) {
	var groupId = $("#ratiosForGroup").attr("groupId")
	var playerName = $("#ratiosForGroup").attr("player")
	$.get(loadRatiosForGroupAction({playerName:playerName, groupId:groupId}), function(data) {
		$("#ratiosForGroup").html(data)
	})
}

/**
 * Contenteditable description container
 */
var buildingSetDescription = #{jsAction @Realm.updateBuildingDescription(':playerName') /}
$('.buildingDescription').live('blur', function() {
	var content = $(this).html();
    var playerName = "${player.name}"
    var buildingId = $(this).attr('buildingId')
    $.post(buildingSetDescription({playerName: playerName}), {content: content, buildingId: buildingId}, function(data) {
    	// no callback needed
    })
})

/**
 * Change deposit quantity
 */
var buildingSetDepositQuantityAction = #{jsAction @Realm.setDepositQuantity(':playerName', ':buildingId', ':depositQuantity') /}
$('.buildingDQSubmit').live('click', function () {
	var deposit = $(this).parent().children("input[name='depositQuantity']").val()
	var root = $(this).parents(".buildingLine")
	var playerName = $(this).attr('playerName')
	var buildingId = $(this).attr('buildingId')
	$.get(buildingSetDepositQuantityAction({playerName: playerName, buildingId: buildingId, depositQuantity: deposit}), function(data) {
		root.find(".buildingDQValue").html(data)
		root.find(".buildingDQValue").addClass("highlight")
		root.find(".buildingDQFormContainer").slideToggle()
	})
	return false
})

/**
 * Change Area
 */
var buildingSetAreaAction = #{jsAction @Realm.setArea(':playerName', ':buildingId', ':area') /}
$('.buildingAreaSubmit').live('click', function () {
	var area = $(this).parent().children("select").val()
	var root = $(this).parents(".buildingLine")
	var playerName = $(this).attr('playerName')
	var buildingId = $(this).attr('buildingId')
	$.get(buildingSetAreaAction({playerName: playerName, buildingId: buildingId, area: area}), function(data) {
		root.find(".buildingAreaValue").html(data)
		root.find(".buildingAreaValue").addClass("highlight")
		root.find(".buildingAreaFormContainer").slideToggle()
	})
	return false
})

/**
 * Change production time
 */
var buildingSetProductionTimeAction = #{jsAction @Realm.setProductionTime(':playerName', ':buildingId', ':productionTimeMinutes', ':productionTimeSeconds') /}
$('.buildingPTSubmit').live('click', function () {
	var pt_m = $(this).parent().children("input[name='productionTimeMinutes']").val()
	var pt_s = $(this).parent().children("input[name='productionTimeSeconds']").val()
	var root = $(this).parents(".buildingLine")
	var playerName = $(this).attr('playerName')
	var buildingId = $(this).attr('buildingId')
	$.get(buildingSetProductionTimeAction({playerName: playerName, buildingId: buildingId, productionTimeMinutes: pt_m, productionTimeSeconds: pt_s}), function(data) {
		root.find(".buildingPTValue").html(data)
		root.find(".buildingPTValue").addClass("highlight")
		root.find(".buildingPTFormContainer").slideToggle()
		// Update ratios
		var update = root.attr("update")
		if(update != null && update == "groups")
			loadRatiosForGroup() 
	})
	return false
})

/**
 * Change group
 */
var buildingAssignGroupAction = #{jsAction @Realm.assignGroup(':playerName', ':buildingId', ':groupId') /}
var buildingRemoveGroupAction = #{jsAction @Realm.removeGroup(':playerName', ':buildingId', ':groupId') /}
$('.buildingGroupSubmit').live('click', function() {
	var root = $(this).parents(".buildingLine")
	var buildingId = $(this).attr("buildingId")
	var playerName = $(this).attr("playerName")
	// Assign to group
	if($(this).hasClass("assign")) {
		var groupId = $(this).parent().children("select[name='assign-group']").val()
		$.get(buildingAssignGroupAction({playerName:playerName, buildingId:buildingId, groupId:groupId}), function(data) {
			root.replaceWith(data)
		})
		// Update ratios
		var update = root.attr("update")
		if(update != null && update == "groups")
			loadRatiosForGroup() 
	}
	// Remove from group
	else if($(this).hasClass("remove")) {
		var groupId = $(this).parent().children("select[name='remove-group']").val()
		$.get(buildingRemoveGroupAction({playerName:playerName, buildingId:buildingId, groupId:groupId}), function(data) {
			root.replaceWith(data)
		})
		// Update ratios
		var update = root.attr("update")
		if(update != null && update == "groups")
			loadRatiosForGroup() 
	}
})

/**
 * Boost, stop, level up, duplicate, validate simulation
 */
var buildingDuplicateAction = #{jsAction @Realm.duplicateBuilding(':playerName', ':buildingId', ':simulated') /}
var buildingSwitchEnableAction = #{jsAction @Realm.switchEnableBuilding(':playerName', ':buildingId') /}
var buildingSwitchBoostAction = #{jsAction @Realm.switchBoostBuilding(':playerName', ':buildingId') /}
var buildingLvlupAction = #{jsAction @Realm.lvlupBuilding(':playerName', ':buildingId') /}
var buildingValidateSimulationAction = #{jsAction @Realm.validateSimulatedBuilding(':playerName', ':buildingId') /}
$('.buildingAction').live('click', function() {
	var playerName = $(this).attr('playerName')
	var buildingId = $(this).attr('buildingId')
	var root = $(this).parents(".buildingListItem")
	var line = $(this).parents(".buildingLine")
	if($(this).hasClass("switchEnable")) {
		$.get(buildingSwitchEnableAction({playerName: playerName, buildingId: buildingId}), function(data) {
			root.replaceWith(data)
			// Update ratios
			var update = line.attr("update")
			if(update != null && update == "groups")
				loadRatiosForGroup() 
		})
	} else if($(this).hasClass("switchBoost")) {
		$.get(buildingSwitchBoostAction({playerName: playerName, buildingId: buildingId}), function(data) {
			root.replaceWith(data)
			// Update ratios
			var update = line.attr("update")
			if(update != null && update == "groups")
				loadRatiosForGroup() 
		})
	} else if($(this).hasClass("lvlup")) {
		$.get(buildingLvlupAction({playerName: playerName, buildingId: buildingId}), function(data) {
			root.replaceWith(data)
			// Update ratios
			var update = line.attr("update")
			if(update != null && update == "groups")
				loadRatiosForGroup() 
		})
	} else if($(this).hasClass("duplicate")) {
		$.get(buildingDuplicateAction({playerName: playerName, buildingId: buildingId, simulated: false}), function(data) {
			$(data).addClass("highlight").insertAfter(line).fadeIn("slow")
			// Update ratios
			var update = line.attr("update")
			if(update != null && update == "groups")
				loadRatiosForGroup() 
		})
	} else if($(this).hasClass("duplicate-simulated")) {
		$.get(buildingDuplicateAction({playerName: playerName, buildingId: buildingId, simulated: true}), function(data) {
			$(data).addClass("highlight").insertAfter(line).fadeIn("slow")
			// Update ratios
			var update = line.attr("update")
			if(update != null && update == "groups")
				loadRatiosForGroup() 
		})
	} else if($(this).hasClass("validateSimulation")) {
		$.get(buildingValidateSimulationAction({playerName: playerName, buildingId: buildingId}), function(data) {
			root.replaceWith(data)
		})
	}
	return false
})

/**
 * Toggle highlighted
 */
$('.buildingLine').live('click', function() {
	$(this).toggleClass('highlight')
})

/**
 * Duplicate building
 * DEPRECATED
 * @param playerName
 * @param buildingId
 */
function building_duplicate(playerName, buildingId) {
	$.get(buildingDuplicateAction({playerName: playerName, buildingId: buildingId}), function(data) {
		$(data).insertAfter($("#buildingPT_" + buildingId)).fadeIn("slow")
	})
}


// We have to keep this one because we use a validation popup and can't use the $(item).live() method
/**
 * Delete building
 */
var buildingDeleteAction = #{jsAction @Realm.deleteBuilding(':playerName', ':buildingId') /}
function building_delete(playerName, buildingId) {
	$.get(buildingDeleteAction({playerName: playerName, buildingId: buildingId}), function(data) {
		$(".building_" + buildingId).fadeOut()
	})
}