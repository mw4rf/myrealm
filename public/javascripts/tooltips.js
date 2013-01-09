/**
 * It should be as simple as :
 * 		$('a[title]').qtip()
 * but it won't work with AJAQ queries, so we have to attach a jQuery live() method
 * for the tooltip to display with content loaded with AJAX
 */
$('a[title]').live("mouseover", function(){
	$('a[title]').each(function(){
		if( $(this).data('qtip') ) { return true; }
		$(this).qtip()
	});
});

$('span[title]').live("mouseover", function(){
	$('span[title]').each(function(){
		$(this).css('cursor','help')
		var my = $(this).attr("my")
		var at = $(this).attr("at")
		if( $(this).data('qtip') ) { return true; }
		$(this).qtip({
			position: {
			      viewport: $(window),
			      my: my,
			      at: at,
			      target: $(this) // my target
			   }
		})
	});
});