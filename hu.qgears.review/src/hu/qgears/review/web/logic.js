/**
 * Initializes mouse listeners on accordion panels. User for instance in page
 * served by HandleSourceSet.
 */
function initAccordion(){
	$('div.accordion h3').click(function(){
	    $(this).next().slideToggle();
	}).next().hide();
}