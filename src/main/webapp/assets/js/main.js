(function($) {
	"use strict"


	// Open cart modal directly from cart dropdown button.
	$('#open-cart-modal').on('click', function (e) {
		e.preventDefault();
		e.stopPropagation();
		$('#cart-modal').modal('show');
	});

	$('#cart-modal').on('shown.bs.modal', function () {
		$('#myInput').trigger('focus')
	})

	// Mobile Nav toggle
	$('.menu-toggle > a').on('click', function (e) {
		e.preventDefault();
		$('#responsive-nav').toggleClass('active');
	})

	// Fix cart dropdown from closing
	$('.cart-dropdown').on('click', function (e) {
		e.stopPropagation();
	});

	/////////////////////////////////////////

	// Products Slick
	$('.products-slick').each(function() {
		var $this = $(this),
				$nav = $this.attr('data-nav');

		$this.slick({
			slidesToShow: 4,
			slidesToScroll: 1,
			autoplay: true,
			infinite: true,
			speed: 300,
			dots: false,
			arrows: true,
			appendArrows: $nav ? $nav : false,
			responsive: [{
	        breakpoint: 991,
	        settings: {
	          slidesToShow: 2,
	          slidesToScroll: 1,
	        }
	      },
	      {
	        breakpoint: 480,
	        settings: {
	          slidesToShow: 1,
	          slidesToScroll: 1,
	        }
	      },
	    ]
		});
	});

	// Products Widget Slick
	$('.products-widget-slick').each(function() {
		var $this = $(this),
				$nav = $this.attr('data-nav');

		$this.slick({
			infinite: true,
			autoplay: true,
			speed: 300,
			dots: false,
			arrows: true,
			appendArrows: $nav ? $nav : false,
		});
	});

	/////////////////////////////////////////

	// Product Main img Slick
	$('#product-main-img').slick({
    infinite: true,
    speed: 300,
    dots: false,
    arrows: true,
    fade: true,
    asNavFor: '#product-imgs',
  });

	// Product imgs Slick
  $('#product-imgs').slick({
    slidesToShow: 3,
    slidesToScroll: 1,
    arrows: true,
    centerMode: true,
    focusOnSelect: true,
		centerPadding: 0,
		vertical: true,
    asNavFor: '#product-main-img',
		responsive: [{
        breakpoint: 991,
        settings: {
					vertical: false,
					arrows: false,
					dots: true,
        }
      },
    ]
  });

	// Product img zoom
	var zoomMainProduct = document.getElementById('product-main-img');
	if (zoomMainProduct) {
		$('#product-main-img .product-preview').zoom();
	}

	/////////////////////////////////////////

	function extractDigits(value) {
		return String(value == null ? '' : value).replace(/\D/g, '');
	}

	function formatVndNumber(value) {
		var digits = extractDigits(value);
		if (!digits) {
			return '';
		}
		return Number(digits).toLocaleString('vi-VN');
	}

	window.extractDigits = extractDigits;
	window.formatVndNumber = formatVndNumber;

	// Input number
	$('.input-number').each(function() {
		var $this = $(this),
		$input = $this.find('input').first(),
		up = $this.find('.qty-up'),
		down = $this.find('.qty-down');

		down.on('click', function () {
			var value = parseInt(extractDigits($input.val()) || '1', 10) - 1;
			value = value < 1 ? 1 : value;
			$input.val(value);
			$input.change();
			// Fix price filter: call updatePriceSlider and updateFilterURL if it's a price input
			updatePriceSlider($this , value);
			if ($this.hasClass('price-min') || $this.hasClass('price-max')) {
				// Delay to allow price slider to update first
				setTimeout(function() {
					if (typeof updateFilterURL === 'function') {
						updateFilterURL();
					}
				}, 100);
			}
		})

		up.on('click', function () {
			var value = parseInt(extractDigits($input.val()) || '0', 10) + 1;
			$input.val(value);
			$input.change();
			// Fix price filter: call updatePriceSlider and updateFilterURL if it's a price input
			updatePriceSlider($this , value);
			if ($this.hasClass('price-min') || $this.hasClass('price-max')) {
				// Delay to allow price slider to update first
				setTimeout(function() {
					if (typeof updateFilterURL === 'function') {
						updateFilterURL();
					}
				}, 100);
			}
		})
	});

	var priceInputMax = document.getElementById('price-max'),
			priceInputMin = document.getElementById('price-min');

	priceInputMax?.addEventListener('change', function(){
		updatePriceSlider($(this).parent() , extractDigits(this.value))
	});

	priceInputMin?.addEventListener('change', function(){
		updatePriceSlider($(this).parent() , extractDigits(this.value))
	});

	function updatePriceSlider(elem , value) {
		if (!priceSlider || !priceSlider.noUiSlider) {
			return;
		}

		if ( elem.hasClass('price-min') ) {
			priceSlider.noUiSlider.set([extractDigits(value), null]);
		} else if ( elem.hasClass('price-max')) {
			priceSlider.noUiSlider.set([null, extractDigits(value)]);
		}
	}

	// Price Slider
	var priceSlider = document.getElementById('price-slider');
	if (priceSlider) {
		var sliderMin = parseInt(priceSlider.getAttribute('data-range-min') || '1', 10);
		var sliderMax = parseInt(priceSlider.getAttribute('data-range-max') || '149999999', 10);
		var initialMin = parseInt(priceSlider.getAttribute('data-selected-min') || String(sliderMin), 10);
		var initialMax = parseInt(priceSlider.getAttribute('data-selected-max') || String(sliderMax), 10);
		var userInteractedWithSlider = false;

		if (Number.isNaN(initialMin)) initialMin = sliderMin;
		if (Number.isNaN(initialMax)) initialMax = sliderMax;
		if (initialMin < sliderMin) initialMin = sliderMin;
		if (initialMax > sliderMax) initialMax = sliderMax;
		if (initialMin > initialMax) initialMin = sliderMin;

		noUiSlider.create(priceSlider, {
			start: [initialMin, initialMax],
			connect: true,
			step: 100000,
			range: {
				'min': sliderMin,
				'max': sliderMax
			}
		});

		if (priceInputMin) priceInputMin.placeholder = String(initialMin);
		if (priceInputMax) priceInputMax.placeholder = String(initialMax);

		// Mark only real user actions; programmatic .set() (restore/sync) won't trigger filter navigation.
		priceSlider.addEventListener('mousedown', function () {
			userInteractedWithSlider = true;
		});
		priceSlider.addEventListener('touchstart', function () {
			userInteractedWithSlider = true;
		}, { passive: true });
		priceSlider.addEventListener('pointerdown', function () {
			userInteractedWithSlider = true;
		});

		priceSlider.noUiSlider.on('update', function( values, handle ) {
			var numericValue = String(Math.round(parseFloat(values[handle])));
			if (handle) {
				if (priceInputMax) {
					priceInputMax.value = numericValue;
					priceInputMax.placeholder = numericValue;
				}
			} else {
				if (priceInputMin) {
					priceInputMin.value = numericValue;
					priceInputMin.placeholder = numericValue;
				}
			}
		});

		priceSlider.noUiSlider.on('set', function() {
			if (!userInteractedWithSlider) {
				return;
			}
			userInteractedWithSlider = false;
			if (typeof window.updateFilterURL === 'function') {
				window.updateFilterURL();
			}
		});
	}


	// Wishlist (đã có)
	document.querySelectorAll(".add-to-wishlist").forEach(btn => {
		btn.addEventListener("click", () => {
			const icon = btn.querySelector("i");
			btn.classList.toggle("active");
			icon.classList.toggle("fa-heart");
			icon.classList.toggle("fa-heart-o");
		});
	});

// Compare
	document.querySelectorAll(".add-to-compare").forEach(btn => {
		btn.addEventListener("click", () => {
			btn.classList.toggle("active");
		});
	});



})(jQuery);
