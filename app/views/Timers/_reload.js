var reloadInterval = 300 // seconds
var infoInterval = 1 // seconds
var reloader, info

var reloadAction = #{jsAction @Timers.list(':playerName', ':actionType') /}

$("#switchAutoreload").live("click", function() {
	// Set variables
	var root = $("#timersRoot")
	var playerName = root.attr("player")
	var status_current = $(this).attr("value")
	var status_alt = $(this).attr("altvalue")
	var started = $(this).attr("started")
	// Setup countdown
	$("#nextReload").html(reloadInterval)
	// Start checkers & countdown
	if(started == 0) {
		// Change button
		$(this).attr("started", 1)
		$(this).removeClass('btn-danger')
		$(this).addClass('btn-success')
		// Start ajax checker
		reloader = setInterval(function() {
			$.get(reloadAction({playerName: playerName, actionType: 0}), function(data) {
				root.html(data)
			})
			$("#nextReload").html(reloadInterval)
		}, reloadInterval*1000)
		// Start countdown
		info = setInterval(function() {
			var count = $("#nextReload").html()
			if(count > 0)
				count--
			$("#nextReload").html(count)
		}, infoInterval*1000)
	// Stop checkers & countdown
	} else {
		clearInterval(reloader)
		clearInterval(info)
		// Change button
		$(this).attr("started", 0)
		$(this).removeClass('btn-success')
		$(this).addClass('btn-danger')
	}
	// Change button
	$(this).attr("value", status_alt)
	$(this).attr("altvalue", status_current)
})