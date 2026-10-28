/**
 * VoyageX Travel Booking - Interactive Frontend Logic
 * Supports dynamic Flight Search, Filtering, Sorting, and Demonstration Booking
 */

/* ================================================================
   AUTH GUARD — runs immediately, before DOMContentLoaded.
   Redirects to /login if the user has no valid session token.
   Uses the token stored in sessionStorage (tab) or localStorage (persist).
   ================================================================ */
(async function voyageXAuthGuard() {
  const SESSION_KEY = 'vx_session_token';
  const USER_KEY = 'vx_session_user';

  const token = sessionStorage.getItem(SESSION_KEY) || localStorage.getItem(SESSION_KEY);

  if (!token) {
    window.location.replace('/login?redirect=1');
    return;
  }

  try {
    const res = await fetch('/api/auth/verify-session?token=' + encodeURIComponent(token));
    if (!res.ok) throw new Error('invalid');
    const data = await res.json();
    if (!data.valid) throw new Error('invalid');
    // Refresh token storage with latest user data
    sessionStorage.setItem(SESSION_KEY, token);
    localStorage.setItem(SESSION_KEY, token);
    if (data.user) {
      const compatUser = {
        id: data.user.id,
        name: data.user.fullName,
        email: data.user.email,
        initials: data.user.initials,
        tier: data.user.membershipTier,
        savedDealIds: data.user.savedDealIds ? Array.from(data.user.savedDealIds) : []
      };
      sessionStorage.setItem(USER_KEY, JSON.stringify(compatUser));
      localStorage.setItem(USER_KEY, JSON.stringify(compatUser));
      localStorage.setItem('voyagex_active_user', JSON.stringify(compatUser));
    }
  } catch (e) {
    // Token invalid/expired — clear and redirect
    sessionStorage.clear();
    localStorage.removeItem(SESSION_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem('voyagex_active_user');
    window.location.replace('/login?expired=1');
  }
})();

document.addEventListener('DOMContentLoaded', () => {
  // --- Form & Core Elements ---
  const originInput = document.getElementById('originInput');

  const destInput = document.getElementById('destInput');
  const swapBtn = document.getElementById('swapBtn');
  const tripTypeRadios = document.querySelectorAll('input[name="tripType"]');
  const returnDateField = document.getElementById('returnDateField');
  const returnDateInput = document.getElementById('returnDate');
  const departureDateInput = document.getElementById('departureDate');
  const cabinClassSelect = document.getElementById('cabinClassSelect');

  const travellersTrigger = document.getElementById('travellersTrigger');
  const travellersDropdown = document.getElementById('travellersDropdown');
  const travellersApplyBtn = document.getElementById('travellersApplyBtn');
  const travellersSummary = document.getElementById('travellersSummary');

  const adultsVal = document.getElementById('adultsVal');
  const childrenVal = document.getElementById('childrenVal');
  const adultsInput = document.getElementById('adultsInput');
  const childrenInput = document.getElementById('childrenInput');

  const searchAlert = document.getElementById('searchAlert');
  const searchAlertText = document.getElementById('searchAlertText');
  const searchForm = document.getElementById('flightSearchForm');

  // --- Results & Filter Elements ---
  const resultsSection = document.getElementById('searchResultsSection');
  const resOrigin = document.getElementById('resOrigin');
  const resDest = document.getElementById('resDest');
  const resCount = document.getElementById('resCount');
  const resClass = document.getElementById('resClass');
  const resDepartDate = document.getElementById('resDepartDate');
  const resPassengers = document.getElementById('resPassengers');

  const airlineFilter = document.getElementById('airlineFilter');
  const nonStopFilter = document.getElementById('nonStopFilter');
  const priceRangeFilter = document.getElementById('priceRangeFilter');
  const priceRangeVal = document.getElementById('priceRangeVal');
  const sortBySelect = document.getElementById('sortBySelect');
  const resetFiltersBtn = document.getElementById('resetFiltersBtn');
  const emptyResetBtn = document.getElementById('emptyResetBtn');

  const flightLoadingState = document.getElementById('flightLoadingState');
  const flightEmptyState = document.getElementById('flightEmptyState');
  const flightErrorState = document.getElementById('flightErrorState');
  const flightErrorMsg = document.getElementById('flightErrorMsg');
  const flightErrorRetryBtn = document.getElementById('flightErrorRetryBtn');
  const flightCardsList = document.getElementById('flightCardsList');

  // --- Booking Modal Elements ---
  const bookingModal = document.getElementById('bookingModal');
  const bookingFormView = document.getElementById('bookingFormView');
  const bookingSuccessView = document.getElementById('bookingSuccessView');
  const modalCloseBtn = document.getElementById('modalCloseBtn');
  const btnCancelBooking = document.getElementById('btnCancelBooking');
  const btnConfirmBooking = document.getElementById('btnConfirmBooking');
  const btnBookingDone = document.getElementById('btnBookingDone');
  const travelerBookingForm = document.getElementById('travelerBookingForm');

  const recapAirlineBadge = document.getElementById('recapAirlineBadge');
  const recapAirlineName = document.getElementById('recapAirlineName');
  const recapFlightMeta = document.getElementById('recapFlightMeta');
  const recapSeats = document.getElementById('recapSeats');
  const recapDepartTime = document.getElementById('recapDepartTime');
  const recapOrigin = document.getElementById('recapOrigin');
  const recapDuration = document.getElementById('recapDuration');
  const recapStops = document.getElementById('recapStops');
  const recapArriveTime = document.getElementById('recapArriveTime');
  const recapDest = document.getElementById('recapDest');
  const recapDate = document.getElementById('recapDate');
  const recapPax = document.getElementById('recapPax');

  const farePaxCount = document.getElementById('farePaxCount');
  const farePaxPlural = document.getElementById('farePaxPlural');
  const fareBaseAmount = document.getElementById('fareBaseAmount');
  const fareTaxesAmount = document.getElementById('fareTaxesAmount');
  const fareTotalAmount = document.getElementById('fareTotalAmount');

  const travelerNameInput = document.getElementById('travelerName');
  const travelerEmailInput = document.getElementById('travelerEmail');
  const travelerPhoneInput = document.getElementById('travelerPhone');

  const confTravelerName = document.getElementById('confTravelerName');
  const confBookingRef = document.getElementById('confBookingRef');
  const confRoute = document.getElementById('confRoute');
  const confFlight = document.getElementById('confFlight');
  const confDepartDate = document.getElementById('confDepartDate');
  const confPassengers = document.getElementById('confPassengers');
  const confTotalFare = document.getElementById('confTotalFare');

  // --- General UI Elements ---
  const promoCopyBtn = document.getElementById('promoCopyBtn');
  const toast = document.getElementById('toastNotice');
  const toastMsg = document.getElementById('toastMsg');

  // --- State Variables ---
  let cachedAirports = [];
  let currentSearchResults = [];
  let currentSelectedFlight = null;
  let currentCalculatedFare = 0;
  let currentSelectedSeat = null;
  let currentSearchParams = {
    origin: 'DEL',
    destination: 'BOM',
    departDate: '',
    returnDate: '',
    passengers: 1,
    cabinClass: 'Economy'
  };

  // --- Currency Conversion Engine ---
  let currentCurrency = 'USD';
  const currencyRates = {
    USD: { symbol: '$', rate: 1.0 },
    EUR: { symbol: '€', rate: 0.92 },
    GBP: { symbol: '£', rate: 0.79 },
    INR: { symbol: '₹', rate: 83.0 }
  };
  function formatPrice(usdAmount) {
    const val = Number(usdAmount) || 0;
    const info = currencyRates[currentCurrency] || currencyRates.USD;
    const converted = Math.round(val * info.rate);
    return `${info.symbol}${converted.toLocaleString()}`;
  }

  // --- Promo Code System ---
  let appliedPromoCode = null;
  let promoDiscountPct = 0;

  window.applyPromoCode = function(code) {
    const norm = (code || '').toUpperCase().trim();
    if (norm === 'VOYAGE30' || norm === 'WELCOME15') {
      appliedPromoCode = norm;
      promoDiscountPct = norm === 'VOYAGE30' ? 0.30 : 0.15;
      const tag = document.getElementById('appliedPromoTag');
      if (tag) {
        tag.style.display = 'inline-flex';
        tag.innerHTML = `${Math.round(promoDiscountPct * 100)}% Off (${appliedPromoCode}) <button type="button" onclick="window.removePromoCode()" style="background:none; border:none; color:#fff; cursor:pointer; font-weight:bold; margin-left:4px;">&times;</button>`;
      }
      showToast(`Promo ${appliedPromoCode} applied! ${Math.round(promoDiscountPct * 100)}% discount on fares.`);
      if (currentSearchResults && currentSearchResults.length > 0) {
        renderFlightCards(currentSearchResults);
      }
      const searchBox = document.getElementById('searchSection');
      if (searchBox) searchBox.scrollIntoView({ behavior: 'smooth' });
    } else {
      showToast('Invalid promo coupon code.');
    }
  };

  window.removePromoCode = function() {
    appliedPromoCode = null;
    promoDiscountPct = 0;
    const tag = document.getElementById('appliedPromoTag');
    if (tag) tag.style.display = 'none';
    showToast('Promo code removed.');
    if (currentSearchResults && currentSearchResults.length > 0) {
      renderFlightCards(currentSearchResults);
    }
  };

  // Currency Selector Dropdown Listener
  const currencySelector = document.getElementById('currencySelector');
  if (currencySelector) {
    currencySelector.addEventListener('change', (e) => {
      currentCurrency = e.target.value;
      showToast(`Currency switched to ${currentCurrency}`);
      if (currentSearchResults && currentSearchResults.length > 0) {
        renderFlightCards(currentSearchResults);
      }
    });
  }

  // --- Demo User State ---
  let currentUser = {
    id: 'user-1',
    name: 'Arun Patel',
    email: 'arun.patel@example.com',
    initials: 'AP',
    tier: 'Gold Flyer'
  };

  // ==========================================
  // 1. Initial Setup & Airport Autocomplete
  // ==========================================
  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  const todayStr = today.toISOString().split('T')[0];
  const tomorrowStr = tomorrow.toISOString().split('T')[0];

  if (departureDateInput) {
    if (!departureDateInput.value) {
      departureDateInput.value = tomorrowStr;
    }
    departureDateInput.min = todayStr;

    departureDateInput.addEventListener('change', () => {
      hideSearchAlert();
      if (returnDateInput && returnDateInput.value && returnDateInput.value < departureDateInput.value) {
        returnDateInput.value = departureDateInput.value;
      }
      if (returnDateInput) {
        returnDateInput.min = departureDateInput.value;
      }
    });
  }

  if (returnDateInput) {
    returnDateInput.min = departureDateInput ? departureDateInput.value : todayStr;
    if (!returnDateInput.value) {
      const nextWeek = new Date(tomorrow);
      nextWeek.setDate(nextWeek.getDate() + 7);
      returnDateInput.value = nextWeek.toISOString().split('T')[0];
    }
    returnDateInput.addEventListener('change', () => hideSearchAlert());
  }

  // Load Airport List from Backend
  fetchAirports();

  async function fetchAirports() {
    try {
      const res = await fetch('/api/flights/airports');
      if (res.ok) {
        cachedAirports = await res.json();
        populateAirportsDatalist(cachedAirports);
      }
    } catch (err) {
      console.warn('Could not load airports list from backend:', err);
    }
  }

  function populateAirportsDatalist(airports) {
    const datalist = document.getElementById('airportsDatalist');
    if (!datalist || !Array.isArray(airports)) return;

    datalist.innerHTML = '';
    airports.forEach(airport => {
      const option = document.createElement('option');
      option.value = `${airport.city} (${airport.code})`;
      option.textContent = `${airport.city} - ${airport.name} (${airport.code}), ${airport.country}`;
      datalist.appendChild(option);
    });
  }

  // Helper to extract 3-letter IATA code from user input
  function extractAirportCode(raw) {
    if (!raw) return '';
    const trimmed = raw.trim();
    // 1. Check parenthesized code e.g. "New Delhi (DEL)"
    const match = trimmed.match(/\(([A-Za-z]{3})\)/);
    if (match) return match[1].toUpperCase();

    // 2. Check exact 3-letter code e.g. "DEL"
    if (/^[A-Za-z]{3}$/.test(trimmed)) {
      return trimmed.toUpperCase();
    }

    // 3. Match against cached airports
    const found = cachedAirports.find(a => 
      (a.city && a.city.toLowerCase() === trimmed.toLowerCase()) ||
      (a.name && a.name.toLowerCase().includes(trimmed.toLowerCase()))
    );
    if (found) return found.code;

    // Fallback: return first 3 characters uppercase
    return trimmed.slice(0, 3).toUpperCase();
  }

  // ==========================================
  // 2. Form Validation & Search Execution
  // ==========================================
  function showSearchAlert(message) {
    if (searchAlert && searchAlertText) {
      searchAlertText.textContent = message;
      searchAlert.classList.add('show');
      searchAlert.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }

  function hideSearchAlert() {
    if (searchAlert) {
      searchAlert.classList.remove('show');
    }
  }

  if (originInput) originInput.addEventListener('input', hideSearchAlert);
  if (destInput) destInput.addEventListener('input', hideSearchAlert);

  function validateSearchInputs() {
    const originRaw = originInput ? originInput.value.trim() : '';
    const destRaw = destInput ? destInput.value.trim() : '';

    if (!originRaw) {
      showSearchAlert('Please specify a departure city or airport.');
      if (originInput) originInput.focus();
      return null;
    }

    if (!destRaw) {
      showSearchAlert('Please specify a destination city or airport.');
      if (destInput) destInput.focus();
      return null;
    }

    const originCode = extractAirportCode(originRaw);
    const destCode = extractAirportCode(destRaw);

    if (!originCode || originCode.length < 3) {
      showSearchAlert('Please enter a valid 3-letter departure airport or city.');
      if (originInput) originInput.focus();
      return null;
    }

    if (!destCode || destCode.length < 3) {
      showSearchAlert('Please enter a valid 3-letter destination airport or city.');
      if (destInput) destInput.focus();
      return null;
    }

    if (originCode === destCode) {
      showSearchAlert('Departure and arrival destinations must be different cities.');
      if (destInput) destInput.focus();
      return null;
    }

    const departDate = departureDateInput ? departureDateInput.value : '';
    if (!departDate) {
      showSearchAlert('Please select a valid departure date.');
      if (departureDateInput) departureDateInput.focus();
      return null;
    }

    if (departDate < todayStr) {
      showSearchAlert('Departure date cannot be in the past. Please choose today or a future date.');
      if (departureDateInput) departureDateInput.focus();
      return null;
    }

    const selectedTripType = document.querySelector('input[name="tripType"]:checked');
    const isRoundTrip = selectedTripType && selectedTripType.value === 'roundTrip';
    const returnDate = returnDateInput ? returnDateInput.value : '';

    if (isRoundTrip && returnDate && returnDate < departDate) {
      showSearchAlert('Return date must be on or after your departure date.');
      if (returnDateInput) returnDateInput.focus();
      return null;
    }

    const adults = parseInt(adultsVal ? adultsVal.textContent : '1', 10);
    const children = parseInt(childrenVal ? childrenVal.textContent : '0', 10);
    const totalPax = adults + children;

    if (totalPax < 1 || totalPax > 9) {
      showSearchAlert('Passenger count must be between 1 and 9 travelers.');
      return null;
    }

    hideSearchAlert();

    return {
      origin: originCode,
      destination: destCode,
      departDate: departDate,
      returnDate: isRoundTrip ? returnDate : '',
      passengers: totalPax,
      cabinClass: cabinClassSelect ? cabinClassSelect.value : 'Economy'
    };
  }

  // Primary Search Trigger
  window.triggerFlightSearch = function() {
    const validated = validateSearchInputs();
    if (!validated) return;

    currentSearchParams = validated;

    // Reset filters for new route search
    if (airlineFilter) airlineFilter.value = 'ALL';
    if (nonStopFilter) nonStopFilter.checked = false;
    if (sortBySelect) sortBySelect.value = 'price_asc';

    // Update Header Display
    if (resOrigin) resOrigin.textContent = validated.origin;
    if (resDest) resDest.textContent = validated.destination;
    if (resDepartDate) resDepartDate.textContent = validated.departDate;
    if (resClass) resClass.textContent = validated.cabinClass;
    if (resPassengers) {
      resPassengers.textContent = `${validated.passengers} Traveler${validated.passengers > 1 ? 's' : ''}`;
    }

    // Reveal results section & scroll
    if (resultsSection) {
      resultsSection.style.display = 'block';
      resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    executeFlightQuery(true);
  };

  // Form submit handler
  if (searchForm) {
    searchForm.addEventListener('submit', (e) => {
      e.preventDefault();
      window.triggerFlightSearch();
    });
  }

  // ==========================================
  // 3. API Query & Results Rendering
  // ==========================================
  async function executeFlightQuery(isNewSearch = false) {
    // Show Loading
    if (flightLoadingState) flightLoadingState.style.display = 'block';
    if (flightEmptyState) flightEmptyState.style.display = 'none';
    if (flightErrorState) flightErrorState.style.display = 'none';
    if (flightCardsList) flightCardsList.innerHTML = '';

    const selectedAirline = airlineFilter ? airlineFilter.value : 'ALL';
    const isNonStopOnly = nonStopFilter ? nonStopFilter.checked : false;
    const selectedSort = sortBySelect ? sortBySelect.value : 'price_asc';
    const maxPrice = priceRangeFilter ? priceRangeFilter.value : '';

    const params = new URLSearchParams({
      origin: currentSearchParams.origin,
      destination: currentSearchParams.destination,
      departDate: currentSearchParams.departDate,
      passengers: currentSearchParams.passengers.toString(),
      cabinClass: currentSearchParams.cabinClass,
      airline: selectedAirline,
      nonStop: isNonStopOnly.toString(),
      sortBy: selectedSort
    });

    if (currentSearchParams.returnDate) {
      params.append('returnDate', currentSearchParams.returnDate);
    }
    if (maxPrice && !isNewSearch) {
      params.append('maxPrice', maxPrice);
    }

    try {
      const res = await fetch(`/api/flights/search?${params.toString()}`);
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || `Server responded with status ${res.status}`);
      }

      const data = await res.json();
      currentSearchResults = data.flights || [];

      // Update airline dropdown on initial route search
      if (isNewSearch && data.availableAirlines) {
        populateAirlineFilter(data.availableAirlines);
      }

      // Update price slider on initial route search
      if (isNewSearch && data.minPrice && data.maxPrice) {
        updatePriceSliderBounds(data.minPrice, data.maxPrice);
      }

      // Hide loading
      if (flightLoadingState) flightLoadingState.style.display = 'none';

      // Update count
      if (resCount) resCount.textContent = data.totalResults || 0;

      if (!currentSearchResults.length) {
        if (flightEmptyState) flightEmptyState.style.display = 'block';
      } else {
        renderFlightCards(currentSearchResults);
      }

    } catch (err) {
      console.error('Flight search error:', err);
      if (flightLoadingState) flightLoadingState.style.display = 'none';
      if (flightErrorState) {
        flightErrorState.style.display = 'block';
        if (flightErrorMsg) {
          flightErrorMsg.textContent = err.message || 'Unable to retrieve flights. Please try again.';
        }
      }
    }
  }

  function populateAirlineFilter(airlines) {
    if (!airlineFilter) return;
    const currentValue = airlineFilter.value;
    airlineFilter.innerHTML = '<option value="ALL">All Airlines</option>';

    airlines.forEach(airline => {
      const opt = document.createElement('option');
      opt.value = airline;
      opt.textContent = airline;
      airlineFilter.appendChild(opt);
    });

    if (airlines.includes(currentValue)) {
      airlineFilter.value = currentValue;
    }
  }

  function updatePriceSliderBounds(minPrice, maxPrice) {
    if (!priceRangeFilter || !priceRangeVal) return;
    const min = Math.max(50, Math.floor(minPrice));
    const max = Math.ceil(maxPrice * 1.05);

    priceRangeFilter.min = min;
    priceRangeFilter.max = max;
    priceRangeFilter.value = max;
    priceRangeVal.textContent = `$${max.toLocaleString()}`;
  }

  // Airline styling helper for badges
  function getAirlineBadgeStyle(airline) {
    const map = {
      'Air India': { bg: '#fee2e2', color: '#b91c1c', code: 'AI' },
      'IndiGo': { bg: '#e0e7ff', color: '#3730a3', code: '6E' },
      'Emirates': { bg: '#fef2f2', color: '#dc2626', code: 'EK' },
      'Singapore Airlines': { bg: '#fef3c7', color: '#92400e', code: 'SQ' },
      'British Airways': { bg: '#e0f2fe', color: '#0369a1', code: 'BA' },
      'Qatar Airways': { bg: '#f3e8ff', color: '#7e22ce', code: 'QR' },
      'Air France': { bg: '#eff6ff', color: '#1d4ed8', code: 'AF' },
      'Lufthansa': { bg: '#fef9c3', color: '#854d0e', code: 'LH' },
      'Delta Air Lines': { bg: '#eff6ff', color: '#1e40af', code: 'DL' }
    };
    return map[airline] || { bg: '#f1f5f9', color: '#334155', code: airline.slice(0, 2).toUpperCase() };
  }

  // Render cards into DOM
  function renderFlightCards(flights) {
    if (!flightCardsList) return;
    flightCardsList.innerHTML = '';

    const pax = currentSearchParams.passengers;

    flights.forEach(f => {
      const badgeStyle = getAirlineBadgeStyle(f.airline);
      const badgeCode = f.airlineCode || badgeStyle.code;
      const origin = f.originCode || f.origin || 'ORIG';
      const dest = f.destinationCode || f.destination || 'DEST';
      const basePrice = f.price !== undefined ? f.price : (f.baseFare || 100);
      const totalFare = f.totalFare !== undefined ? f.totalFare : (basePrice * pax);
      const finalTotalFare = promoDiscountPct > 0 ? Math.round(totalFare * (1 - promoDiscountPct)) : totalFare;
      const seatsLeft = f.seatsAvailable !== undefined ? f.seatsAvailable : (f.availableSeats !== undefined ? f.availableSeats : 6);
      const seatsAlertClass = seatsLeft <= 4 ? ' style="background:#fef2f2; color:#dc2626;"' : '';

      const stopsHtml = f.isNonStop
        ? '<div class="stops-label">Non-stop</div>'
        : `<div class="stops-label" style="color:#d97706;" title="${f.layoverInfo || '1 stop'}">${f.layoverInfo || '1 stop'}</div>`;

      let recBadgeHtml = '';
      if (f.recommendationCategory === 'BEST_OVERALL') {
        recBadgeHtml = '<span class="card-badge" style="background:#10b981; color:#fff; font-size:0.72rem; padding:0.15rem 0.45rem; border-radius:4px; margin-bottom:4px; display:inline-block;">🌟 Best Overall Pick</span>';
      } else if (f.recommendationCategory === 'CHEAPEST') {
        recBadgeHtml = '<span class="card-badge" style="background:#2563eb; color:#fff; font-size:0.72rem; padding:0.15rem 0.45rem; border-radius:4px; margin-bottom:4px; display:inline-block;">💰 Lowest Price</span>';
      } else if (f.recommendationCategory === 'FASTEST') {
        recBadgeHtml = '<span class="card-badge" style="background:#f59e0b; color:#fff; font-size:0.72rem; padding:0.15rem 0.45rem; border-radius:4px; margin-bottom:4px; display:inline-block;">⚡ Fastest Route</span>';
      } else if (f.recommendationCategory === 'POPULAR') {
        recBadgeHtml = '<span class="card-badge" style="background:#8b5cf6; color:#fff; font-size:0.72rem; padding:0.15rem 0.45rem; border-radius:4px; margin-bottom:4px; display:inline-block;">🏢 Popular Airline</span>';
      } else if (f.recommendationCategory === 'PREMIUM') {
        recBadgeHtml = '<span class="card-badge" style="background:#ec4899; color:#fff; font-size:0.72rem; padding:0.15rem 0.45rem; border-radius:4px; margin-bottom:4px; display:inline-block;">👑 Premium Experience</span>';
      }

      let priceHtml = `<div class="flight-price-val">${formatPrice(finalTotalFare)}</div>`;
      if (promoDiscountPct > 0) {
        priceHtml = `
          <div class="flight-price-val" style="color: #ec4899;">
            ${formatPrice(finalTotalFare)}
            <span style="font-size: 0.8rem; text-decoration: line-through; color: var(--text-muted); font-weight: 500; margin-left: 4px;">
              ${formatPrice(totalFare)}
            </span>
          </div>
        `;
      }

      const farePerPerson = pax > 1
        ? `<div class="flight-price-sub">${formatPrice(basePrice)} / person &bull; Total for ${pax}</div>`
        : `<div class="flight-price-sub">${f.cabinClass || 'Economy'}${promoDiscountPct > 0 ? ' &bull; 30% Off' : ' &bull; Taxes inc.'}</div>`;

      const card = document.createElement('div');
      card.className = 'flight-card';
      card.setAttribute('data-flight-id', f.id);
      card.innerHTML = `
        <div class="flight-airline">
          <div class="airline-badge" style="background:${badgeStyle.bg}; color:${badgeStyle.color};">
            ${badgeCode}
          </div>
          <div class="airline-meta">
            ${recBadgeHtml}
            <h4>${f.airline}</h4>
            <span>${f.flightNumber} &bull; ${f.aircraft}</span>
            <div style="margin-top: 4px;">
              <span class="seats-badge"${seatsAlertClass}>${seatsLeft} seats left</span>
            </div>
            <div style="display: flex; gap: 0.35rem; flex-wrap: wrap; margin-top: 6px;">
              <span class="flight-badge-baggage" title="Baggage Allowance">🧳 ${f.baggageAllowance || 'Cabin: 7kg • Checked: 23kg'}</span>
              <span class="flight-badge-condition" title="Fare Conditions">🛡️ ${f.fareConditions || 'Complimentary meals • Flexible'}</span>
            </div>
          </div>
        </div>

        <div class="flight-timeline">
          <div class="time-box">
            <h5>${f.departureTime}</h5>
            <span>${origin}</span>
          </div>
          <div class="flight-duration-track">
            <div class="duration-label">${f.duration}</div>
            <div class="duration-line"><div class="duration-plane-icon">✈</div></div>
            ${stopsHtml}
          </div>
          <div class="time-box">
            <h5>${f.arrivalTime}</h5>
            <span>${dest}</span>
          </div>
        </div>

        <div class="flight-price-box">
          ${priceHtml}
          ${farePerPerson}
        </div>

        <div style="display: flex; flex-direction: column; gap: 0.4rem; min-width: 140px;">
          <button type="button" class="btn btn-primary" onclick="window.selectFlight('${f.id}')" aria-label="Select flight ${f.airline} ${f.flightNumber}" style="padding: 0.5rem 0.85rem; font-size: 0.88rem;">
            Select Flight
          </button>
          <div style="display: flex; gap: 0.35rem; justify-content: center;">
            <button type="button" class="btn btn-outline" onclick="window.openSeatMapForFlight('${f.flightNumber}', '${f.aircraft}')" style="padding: 0.25rem 0.5rem; font-size: 0.76rem;" title="Interactive Aircraft Cabin Seat Selection">
              💺 Seats
            </button>
            <button type="button" class="btn btn-outline" onclick="window.openPriceTrendsForRoute('${origin}', '${dest}')" style="padding: 0.25rem 0.5rem; font-size: 0.76rem;" title="7-Day Price History & Fare Freeze">
              📊 Trends
            </button>
          </div>
        </div>
      `;

      flightCardsList.appendChild(card);
    });
  }

  // --- Flight Recommendation Category Tabs Engine ---
  let currentFlightRecTab = 'ALL';
  const flightRecTabs = document.getElementById('flightRecTabs');
  if (flightRecTabs) {
    flightRecTabs.querySelectorAll('.flight-rec-tab').forEach(tab => {
      tab.addEventListener('click', () => {
        flightRecTabs.querySelectorAll('.flight-rec-tab').forEach(t => {
          t.classList.remove('active');
          t.setAttribute('aria-selected', 'false');
        });
        tab.classList.add('active');
        tab.setAttribute('aria-selected', 'true');
        currentFlightRecTab = tab.getAttribute('data-cat') || 'ALL';
        applyFlightRecommendationTab();
      });
    });
  }

  function parseDurationToMinutes(durStr) {
    if (!durStr) return 9999;
    const match = durStr.match(/(?:(\d+)h)?\s*(?:(\d+)m)?/);
    if (!match) return 9999;
    const hours = parseInt(match[1] || '0', 10);
    const minutes = parseInt(match[2] || '0', 10);
    return hours * 60 + minutes;
  }

  function applyFlightRecommendationTab() {
    if (!currentSearchResults || currentSearchResults.length === 0) return;
    let filtered = [...currentSearchResults];

    if (currentFlightRecTab === 'CHEAPEST') {
      filtered.sort((a, b) => (a.price || a.baseFare || 0) - (b.price || b.baseFare || 0));
    } else if (currentFlightRecTab === 'FASTEST') {
      filtered.sort((a, b) => parseDurationToMinutes(a.duration) - parseDurationToMinutes(b.duration));
    } else if (currentFlightRecTab === 'BEST_OVERALL') {
      filtered.sort((a, b) => {
        if (a.recommendationCategory === 'BEST_OVERALL') return -1;
        if (b.recommendationCategory === 'BEST_OVERALL') return 1;
        return (a.price || 0) - (b.price || 0);
      });
    } else if (currentFlightRecTab === 'NONSTOP') {
      filtered = filtered.filter(f => f.isNonStop || f.stopsCount === 0);
    } else if (currentFlightRecTab === 'POPULAR') {
      const topAirlines = ['Emirates', 'Air India', 'Singapore Airlines', 'British Airways', 'Qatar Airways'];
      filtered = filtered.filter(f => topAirlines.includes(f.airline) || f.recommendationCategory === 'POPULAR');
    } else if (currentFlightRecTab === 'PREMIUM') {
      filtered = filtered.filter(f => f.recommendationCategory === 'PREMIUM' || (f.cabinClass && f.cabinClass !== 'Economy') || (f.price > 500));
    }

    if (filtered.length === 0) {
      if (flightEmptyState) {
        flightEmptyState.style.display = 'block';
        const msg = document.getElementById('emptyStateMsg');
        if (msg) msg.textContent = `No flights matched the "${currentFlightRecTab.replace('_', ' ')}" recommendation filter for this route.`;
      }
      if (flightCardsList) flightCardsList.innerHTML = '';
    } else {
      if (flightEmptyState) flightEmptyState.style.display = 'none';
      renderFlightCards(filtered);
    }
  }

  // ==========================================
  // 4. Filters & Sorting Event Listeners
  // ==========================================
  if (airlineFilter) {
    airlineFilter.addEventListener('change', () => executeFlightQuery(false));
  }

  if (nonStopFilter) {
    nonStopFilter.addEventListener('change', () => executeFlightQuery(false));
  }

  if (sortBySelect) {
    sortBySelect.addEventListener('change', () => executeFlightQuery(false));
  }

  if (priceRangeFilter) {
    priceRangeFilter.addEventListener('input', (e) => {
      if (priceRangeVal) {
        priceRangeVal.textContent = `$${Number(e.target.value).toLocaleString()}`;
      }
    });
    priceRangeFilter.addEventListener('change', () => executeFlightQuery(false));
  }

  function resetFiltersAndSearch() {
    if (airlineFilter) airlineFilter.value = 'ALL';
    if (nonStopFilter) nonStopFilter.checked = false;
    if (sortBySelect) sortBySelect.value = 'price_asc';
    if (priceRangeFilter) {
      priceRangeFilter.value = priceRangeFilter.max;
      if (priceRangeVal) priceRangeVal.textContent = `$${Number(priceRangeFilter.max).toLocaleString()}`;
    }
    executeFlightQuery(false);
  }

  if (resetFiltersBtn) resetFiltersBtn.addEventListener('click', resetFiltersAndSearch);
  if (emptyResetBtn) emptyResetBtn.addEventListener('click', resetFiltersAndSearch);
  if (flightErrorRetryBtn) flightErrorRetryBtn.addEventListener('click', () => executeFlightQuery(true));

  // ==========================================
  // 5. Select Flight & Booking Confirmation
  // ==========================================
  window.selectFlight = function(flightId) {
    const flight = currentSearchResults.find(f => f.id === flightId);
    if (!flight) {
      showToast('Flight details not found. Please try searching again.');
      return;
    }

    currentSelectedFlight = flight;

    const pax = currentSearchParams.passengers;
    const basePrice = flight.price !== undefined ? flight.price : (flight.baseFare || 100);
    const baseFareTotal = basePrice * pax;
    const discountAmount = promoDiscountPct > 0 ? Math.round(baseFareTotal * promoDiscountPct) : 0;
    const discountedBase = baseFareTotal - discountAmount;
    const taxesTotal = Math.round(discountedBase * 0.12);
    currentCalculatedFare = discountedBase + taxesTotal;

    // Populate Recap Box
    const badgeStyle = getAirlineBadgeStyle(flight.airline);
    const badgeCode = flight.airlineCode || badgeStyle.code;
    const origin = flight.originCode || flight.origin || 'ORIG';
    const dest = flight.destinationCode || flight.destination || 'DEST';
    const seatsLeft = flight.seatsAvailable !== undefined ? flight.seatsAvailable : (flight.availableSeats || 5);

    if (recapAirlineBadge) {
      recapAirlineBadge.textContent = badgeCode;
      recapAirlineBadge.style.background = badgeStyle.bg;
      recapAirlineBadge.style.color = badgeStyle.color;
    }
    if (recapAirlineName) recapAirlineName.textContent = flight.airline;
    if (recapFlightMeta) recapFlightMeta.textContent = `${flight.flightNumber} • ${flight.aircraft}`;
    if (recapSeats) recapSeats.textContent = `${seatsLeft} seats left`;

    if (recapDepartTime) recapDepartTime.textContent = flight.departureTime;
    if (recapOrigin) recapOrigin.textContent = origin;
    if (recapDuration) recapDuration.textContent = flight.duration;
    if (recapStops) {
      recapStops.textContent = flight.layoverInfo || (flight.isNonStop ? 'Non-stop' : '1 stop');
    }
    if (recapArriveTime) {
      recapArriveTime.textContent = flight.arrivalTime;
    }
    if (recapDest) recapDest.textContent = dest;
    if (recapDate) recapDate.textContent = currentSearchParams.departDate;
    if (recapPax) recapPax.textContent = `${pax} Traveler${pax > 1 ? 's' : ''}`;

    // Populate Fare Breakdown Table
    if (farePaxCount) farePaxCount.textContent = pax;
    if (farePaxPlural) farePaxPlural.textContent = pax > 1 ? 's' : '';
    if (fareBaseAmount) fareBaseAmount.textContent = formatPrice(discountedBase);
    if (fareTaxesAmount) fareTaxesAmount.textContent = formatPrice(taxesTotal);
    if (fareTotalAmount) fareTotalAmount.textContent = formatPrice(currentCalculatedFare);

    // Setup Seat Selection hook in Booking Dialog
    const btnChooseSeat = document.getElementById('btnChooseSeatInBooking');
    if (btnChooseSeat) {
      btnChooseSeat.onclick = () => {
        window.openSeatMapForFlight(flight.flightNumber, flight.aircraft);
      };
    }
    const seatBadge = document.getElementById('bookingSelectedSeatBadge');
    if (seatBadge) {
      if (currentSelectedSeat) {
        seatBadge.style.display = 'block';
        const seatVal = document.getElementById('bookingSelectedSeatVal');
        const seatTier = document.getElementById('bookingSelectedSeatTier');
        if (seatVal) seatVal.textContent = currentSelectedSeat.seatCode;
        if (seatTier) seatTier.textContent = currentSelectedSeat.tier;
      } else {
        seatBadge.style.display = 'none';
      }
    }

    // Switch view to form
    if (bookingFormView) bookingFormView.style.display = 'block';
    if (bookingSuccessView) bookingSuccessView.style.display = 'none';

    // Show Modal
    if (bookingModal) {
      bookingModal.classList.add('open');
      if (travelerNameInput) travelerNameInput.focus();
    }
  };

  function closeBookingModal() {
    if (bookingModal) {
      bookingModal.classList.remove('open');
    }
  }

  if (modalCloseBtn) modalCloseBtn.addEventListener('click', closeBookingModal);
  if (btnCancelBooking) btnCancelBooking.addEventListener('click', closeBookingModal);

  // Close modal when clicking outside
  if (bookingModal) {
    bookingModal.addEventListener('click', (e) => {
      if (e.target === bookingModal) {
        closeBookingModal();
      }
    });
  }

  // Close on Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && bookingModal && bookingModal.classList.contains('open')) {
      closeBookingModal();
    }
  });

  // Booking Form Submission to Backend REST Endpoint
  if (travelerBookingForm) {
    travelerBookingForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      if (!currentSelectedFlight) return;

      const name = travelerNameInput ? travelerNameInput.value.trim() : '';
      const email = travelerEmailInput ? travelerEmailInput.value.trim() : '';
      const phone = travelerPhoneInput ? travelerPhoneInput.value.trim() : '';

      if (!name || !email || !phone) {
        showToast('Please fill in all traveler contact fields.');
        return;
      }

      if (btnConfirmBooking) {
        btnConfirmBooking.disabled = true;
        btnConfirmBooking.textContent = 'Reserving Itinerary...';
      }

      const payload = {
        flightId: currentSelectedFlight.id,
        airline: currentSelectedFlight.airline,
        flightNumber: currentSelectedFlight.flightNumber,
        origin: currentSelectedFlight.originCode || currentSelectedFlight.origin,
        destination: currentSelectedFlight.destinationCode || currentSelectedFlight.destination,
        departureDate: currentSearchParams.departDate,
        passengers: currentSearchParams.passengers,
        totalFare: currentCalculatedFare,
        passengerName: name,
        passengerEmail: email,
        passengerPhone: phone
      };

      try {
        const res = await fetch('/api/flights/book', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        });

        if (!res.ok) {
          const errData = await res.json().catch(() => ({}));
          throw new Error(errData.message || 'Failed to complete demonstration reservation.');
        }

        const data = await res.json();

        // Populate Success Confirmation Card
        if (confTravelerName) confTravelerName.textContent = data.passengerName || name;
        if (confBookingRef) confBookingRef.textContent = data.bookingReference || 'VX-DEMO';
        if (confRoute) confRoute.textContent = `${data.origin} → ${data.destination}`;
        if (confFlight) confFlight.textContent = `${data.airline} (${data.flightNumber})`;
        if (confDepartDate) confDepartDate.textContent = data.departureDate;
        if (confPassengers) {
          confPassengers.textContent = `${data.passengers} Traveler${data.passengers > 1 ? 's' : ''}`;
        }
        if (confTotalFare) confTotalFare.textContent = `$${Number(data.totalFare).toLocaleString()}`;

        // Switch to Confirmation View
        if (bookingFormView) bookingFormView.style.display = 'none';
        if (bookingSuccessView) bookingSuccessView.style.display = 'block';

        showToast(`Demonstration booking confirmed! Ref: ${data.bookingReference}`);

      } catch (err) {
        console.error('Booking submission error:', err);
        showToast(`Booking error: ${err.message}`);
      } finally {
        if (btnConfirmBooking) {
          btnConfirmBooking.disabled = false;
          btnConfirmBooking.textContent = 'Confirm Demonstration Booking';
        }
      }
    });
  }

  // Finish Booking & Reset
  if (btnBookingDone) {
    btnBookingDone.addEventListener('click', () => {
      closeBookingModal();
      const searchBox = document.getElementById('searchSection');
      if (searchBox) {
        searchBox.scrollIntoView({ behavior: 'smooth' });
      }
    });
  }

  // ==========================================
  // 6. Existing UI Hooks & Helpers
  // ==========================================
  // Swap Origin & Destination
  if (swapBtn && originInput && destInput) {
    swapBtn.addEventListener('click', () => {
      const temp = originInput.value;
      originInput.value = destInput.value;
      destInput.value = temp;
      hideSearchAlert();
      showToast('Swapped departure and destination');
    });
  }

  // Trip Type Switch (Round Trip vs One Way vs Multi-City)
  tripTypeRadios.forEach(radio => {
    radio.addEventListener('change', (e) => {
      const multiCityRow = document.getElementById('multiCityRow');
      if (e.target.value === 'multiCity') {
        if (multiCityRow) multiCityRow.style.display = 'grid';
        if (returnDateField) {
          returnDateField.style.display = 'none';
          if (returnDateInput) returnDateInput.disabled = true;
        }
        showToast('Multi-City route enabled. Configure Leg 1 and Leg 2.');
      } else if (e.target.value === 'oneWay') {
        if (multiCityRow) multiCityRow.style.display = 'none';
        if (returnDateField) {
          returnDateField.style.display = 'block';
          returnDateField.style.opacity = '0.4';
          if (returnDateInput) {
            returnDateInput.disabled = true;
            returnDateInput.value = '';
          }
        }
      } else {
        // Round trip
        if (multiCityRow) multiCityRow.style.display = 'none';
        if (returnDateField) {
          returnDateField.style.display = 'block';
          returnDateField.style.opacity = '1';
          if (returnDateInput) {
            returnDateInput.disabled = false;
            if (!returnDateInput.value && departureDateInput && departureDateInput.value) {
              const dep = new Date(departureDateInput.value);
              dep.setDate(dep.getDate() + 7);
              returnDateInput.value = dep.toISOString().split('T')[0];
            }
          }
        }
      }
    });
  });

  // Travellers Stepper Dropdown
  if (travellersTrigger && travellersDropdown) {
    travellersTrigger.addEventListener('click', (e) => {
      e.stopPropagation();
      travellersDropdown.classList.toggle('open');
    });

    document.addEventListener('click', (e) => {
      if (!travellersDropdown.contains(e.target) && !travellersTrigger.contains(e.target)) {
        travellersDropdown.classList.remove('open');
      }
    });

    if (travellersApplyBtn) {
      travellersApplyBtn.addEventListener('click', () => {
        updateTravellersDisplay();
        travellersDropdown.classList.remove('open');
      });
    }

    document.querySelectorAll('.stepper-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const type = btn.getAttribute('data-type');
        const action = btn.getAttribute('data-action');
        
        let adults = parseInt(adultsVal.textContent, 10);
        let children = parseInt(childrenVal.textContent, 10);

        if (type === 'adults') {
          if (action === 'plus' && adults < 9) adults++;
          if (action === 'minus' && adults > 1) adults--;
          adultsVal.textContent = adults;
          adultsInput.value = adults;
        } else if (type === 'children') {
          if (action === 'plus' && children < 6) children++;
          if (action === 'minus' && children > 0) children--;
          childrenVal.textContent = children;
          childrenInput.value = children;
        }

        updateTravellersDisplay();
      });
    });
  }

  function updateTravellersDisplay() {
    const adults = parseInt(adultsVal ? adultsVal.textContent : '1', 10);
    const children = parseInt(childrenVal ? childrenVal.textContent : '0', 10);
    const total = adults + children;
    const label = total === 1 ? '1 Traveller' : `${total} Travellers`;
    if (travellersSummary) {
      travellersSummary.textContent = label;
    }
  }

  // Destination Card Autocomplete & Instant Exploration
  window.selectDestination = function(cityName) {
    if (destInput) {
      destInput.value = cityName;
      hideSearchAlert();
      const searchBox = document.getElementById('searchSection');
      if (searchBox) {
        searchBox.scrollIntoView({ behavior: 'smooth' });
      }
      destInput.focus();
      showToast(`Selected destination: ${cityName}`);
    }
  };

  window.exploreDestination = function(cityName) {
    if (destInput) {
      destInput.value = cityName;
      hideSearchAlert();
      const searchBox = document.getElementById('searchSection');
      if (searchBox) {
        searchBox.scrollIntoView({ behavior: 'smooth' });
      }
      showToast(`Exploring flights to ${cityName}...`);
      setTimeout(() => {
        if (window.triggerFlightSearch) {
          window.triggerFlightSearch();
        }
      }, 350);
    }
  };

  // Promo Code Quick Copy
  if (promoCopyBtn) {
    promoCopyBtn.addEventListener('click', () => {
      const code = 'VOYAGE30';
      navigator.clipboard.writeText(code).then(() => {
        showToast('Promo code VOYAGE30 copied! Applying discount...');
        window.applyPromoCode(code);
      }).catch(() => {
        window.applyPromoCode(code);
      });
    });
  }

  // Toast Notification Utility
  function showToast(message) {
    if (!toast || !toastMsg) return;
    toastMsg.textContent = message;
    toast.classList.add('show');
    setTimeout(() => {
      toast.classList.remove('show');
    }, 3200);
  }

  // Mobile Navigation Toggle
  const mobileToggle = document.getElementById('mobileMenuToggle');
  const navLinks = document.getElementById('navLinks');
  if (mobileToggle && navLinks) {
    mobileToggle.addEventListener('click', () => {
      const isVisible = navLinks.style.display === 'flex';
      navLinks.style.display = isVisible ? 'none' : 'flex';
      if (!isVisible) {
        navLinks.style.flexDirection = 'column';
        navLinks.style.position = 'absolute';
        navLinks.style.top = '72px';
        navLinks.style.left = '0';
        navLinks.style.right = '0';
        navLinks.style.background = '#0a1128';
        navLinks.style.padding = '1.5rem';
        navLinks.style.borderBottom = '1px solid rgba(255, 255, 255, 0.1)';
      }
    });
  }

  // ==========================================
  // MODULE 1: Live Flight Status & ETA Tracker
  // ==========================================
  let cachedFlightStatuses = [];
  let currentStatusFilter = 'ALL';

  async function loadLiveFlightStatus(filterStatus = 'ALL', query = '') {
    const grid = document.getElementById('flightStatusGrid');
    if (!grid) return;

    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">Loading real-time flight operations...</div>';

    try {
      let url = '/api/flight-status';
      if (filterStatus && filterStatus !== 'ALL') {
        url = `/api/flight-status/filter?status=${encodeURIComponent(filterStatus)}`;
      } else if (query && query.trim() !== '') {
        url = `/api/flight-status/search?query=${encodeURIComponent(query.trim())}`;
      }

      const res = await fetch(url);
      if (!res.ok) throw new Error('Failed to fetch flight status');
      const data = await res.json();
      cachedFlightStatuses = data;
      renderFlightStatusCards(data);
    } catch (err) {
      grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: #ef4444;">Unable to load live flight status: ${err.message}</div>`;
    }
  }

  function renderFlightStatusCards(flights) {
    const grid = document.getElementById('flightStatusGrid');
    if (!grid) return;

    if (!flights || flights.length === 0) {
      grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">No flights matching this filter.</div>';
      return;
    }

    grid.innerHTML = '';
    flights.forEach(f => {
      let badgeClass = 'status-ontime';
      let statusLabel = 'ON TIME';
      if (f.status === 'DELAYED') {
        badgeClass = 'status-delayed';
        statusLabel = `DELAYED (${f.delayMinutes}m)`;
      } else if (f.status === 'BOARDING') {
        badgeClass = 'status-boarding';
        statusLabel = 'BOARDING';
      } else if (f.status === 'DEPARTED') {
        badgeClass = 'status-departed';
        statusLabel = 'IN FLIGHT';
      } else if (f.status === 'LANDED') {
        badgeClass = 'status-landed';
        statusLabel = 'LANDED';
      }

      const delayAlertHtml = f.status === 'DELAYED' && f.delayReason
        ? `<div class="status-delay-alert">⚠️ <strong>Notice:</strong> ${f.delayReason}. Revised departure: ${f.revisedDepartureTime}.</div>`
        : '';

      const card = document.createElement('div');
      card.className = 'status-flight-card';
      card.innerHTML = `
        <div class="status-card-header">
          <div>
            <h4 style="margin: 0; font-size: 1.05rem; font-weight: 700; color: var(--secondary);">${f.airline}</h4>
            <span style="font-size: 0.8rem; color: var(--text-muted);">${f.flightNumber} &bull; ${f.aircraft}</span>
          </div>
          <span class="status-badge ${badgeClass}">${statusLabel}</span>
        </div>

        <div class="status-route-row" style="display: flex; justify-content: space-between; align-items: center; margin: 0.85rem 0;">
          <div style="text-align: left;">
            <div style="font-size: 1.25rem; font-weight: 800; color: var(--secondary);">${f.scheduledDepartureTime}</div>
            <div style="font-size: 0.85rem; font-weight: 600; color: var(--primary);">${f.origin}</div>
          </div>
          <div style="font-size: 1.2rem; color: var(--text-muted);">✈ &rarr;</div>
          <div style="text-align: right;">
            <div style="font-size: 1.25rem; font-weight: 800; color: var(--secondary);">${f.scheduledArrivalTime}</div>
            <div style="font-size: 0.85rem; font-weight: 600; color: var(--primary);">${f.destination}</div>
          </div>
        </div>

        ${delayAlertHtml}

        <div class="status-airport-meta">
          <div><span style="color:var(--text-muted);font-size:0.75rem;">Terminal:</span> <strong style="font-size:0.85rem;">${f.terminal}</strong></div>
          <div><span style="color:var(--text-muted);font-size:0.75rem;">Gate:</span> <strong style="font-size:0.85rem;">${f.gate}</strong></div>
          <div><span style="color:var(--text-muted);font-size:0.75rem;">Baggage:</span> <strong style="font-size:0.85rem;">${f.baggageCarousel}</strong></div>
        </div>

        <div style="margin-top: 1rem; border-top: 1px solid var(--border); padding-top: 0.75rem; display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 0.75rem; color: var(--text-muted);">Live Radar Updates</span>
          <button type="button" class="btn btn-outline" style="font-size: 0.78rem; padding: 0.35rem 0.75rem;" onclick="window.subscribeFlightAlerts('${f.flightNumber}')">
            🔔 Get SMS/Email Alert
          </button>
        </div>
      `;
      grid.appendChild(card);
    });
  }

  // Flight Status Filter Pills & Search
  const statusFilterPills = document.querySelectorAll('.status-filter-btn');
  statusFilterPills.forEach(btn => {
    btn.addEventListener('click', () => {
      statusFilterPills.forEach(b => {
        b.classList.remove('active');
        b.classList.remove('btn-primary');
        b.classList.add('btn-outline');
      });
      btn.classList.add('active');
      btn.classList.add('btn-primary');
      btn.classList.remove('btn-outline');
      currentStatusFilter = btn.getAttribute('data-filter') || 'ALL';
      loadLiveFlightStatus(currentStatusFilter, '');
    });
  });

  const statusSearchInput = document.getElementById('statusSearchInput');
  if (statusSearchInput) {
    statusSearchInput.addEventListener('input', (e) => {
      const q = e.target.value;
      loadLiveFlightStatus(currentStatusFilter, q);
    });
  }

  const btnRefreshStatus = document.getElementById('btnRefreshStatus');
  if (btnRefreshStatus) {
    btnRefreshStatus.addEventListener('click', () => {
      loadLiveFlightStatus(currentStatusFilter, statusSearchInput ? statusSearchInput.value : '');
      showToast('Refreshed real-time flight operations radar.');
    });
  }

  window.subscribeFlightAlerts = async function(flightNumber) {
    const email = currentUser ? currentUser.email : 'traveler@example.com';
    try {
      const res = await fetch('/api/flight-status/notify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ flightNumber: flightNumber, email: email, phone: '+91 98765 43210' })
      });
      if (res.ok) {
        showToast(`Subscribed for instant gate & delay alerts for ${flightNumber}!`);
      } else {
        showToast(`Subscribed for alerts for ${flightNumber}.`);
      }
    } catch {
      showToast(`Subscribed for alerts for ${flightNumber}.`);
    }
  };

  // ==========================================
  // MODULE 2: Dynamic Pricing & 48h Price Freeze
  // ==========================================
  const pricingTrendModal = document.getElementById('pricingTrendModal');
  const pricingModalCloseBtn = document.getElementById('pricingModalCloseBtn');
  const btnOpenPriceTrends = document.getElementById('btnOpenPriceTrends');
  const btnFreezeFareNow = document.getElementById('btnFreezeFareNow');

  let activePricingHistory = null;
  let activeFreezeQuote = null;

  window.openPriceTrendsForRoute = async function(origin, dest) {
    if (!origin) origin = originInput ? originInput.value.split('(')[1]?.replace(')', '') || 'DEL' : 'DEL';
    if (!dest) dest = destInput ? destInput.value.split('(')[1]?.replace(')', '') || 'BOM' : 'BOM';

    const modalTitle = document.getElementById('pricingModalTitle');
    const modalSubtitle = document.getElementById('pricingModalSubtitle');
    const barsContainer = document.getElementById('priceTrendBars');
    const demandBadge = document.getElementById('pricingDemandBadge');
    const adviceText = document.getElementById('pricingAdviceText');
    const freezeSuccessBox = document.getElementById('freezeSuccessBox');

    if (freezeSuccessBox) freezeSuccessBox.style.display = 'none';
    if (modalTitle) modalTitle.textContent = `Fare Intelligence: ${origin} → ${dest}`;
    if (modalSubtitle) modalSubtitle.textContent = `Simulated 7-Day Trend Analysis • Real-time Demand Engine`;

    if (barsContainer) {
      barsContainer.innerHTML = '<div style="text-align: center; width: 100%; color: var(--text-muted);">Analyzing route volatility...</div>';
    }

    if (pricingTrendModal) pricingTrendModal.classList.add('open');

    try {
      const res = await fetch(`/api/pricing/history?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(dest)}`);
      if (!res.ok) throw new Error('Failed to retrieve pricing history');
      const data = await res.json();
      activePricingHistory = data;

      if (barsContainer) {
        barsContainer.innerHTML = '';
        const maxPrice = Math.max(...data.map(d => d.fare), 300);
        data.forEach(pt => {
          const barHeightPct = Math.max(25, Math.round((pt.fare / maxPrice) * 100));
          let barBg = '#3b82f6';
          if (pt.trendLabel === 'SURGE') barBg = '#ef4444';
          if (pt.trendLabel === 'LOW') barBg = '#10b981';

          const barWrapper = document.createElement('div');
          barWrapper.className = 'price-trend-col';
          barWrapper.style.flex = '1';
          barWrapper.style.display = 'flex';
          barWrapper.style.flexDirection = 'column';
          barWrapper.style.alignItems = 'center';
          barWrapper.style.justifyContent = 'flex-end';
          barWrapper.style.height = '100%';
          barWrapper.innerHTML = `
            <div style="font-size: 0.72rem; font-weight: 700; color: var(--secondary); margin-bottom: 4px;">${formatPrice(pt.fare)}</div>
            <div style="width: 100%; height: ${barHeightPct}%; background: ${barBg}; border-radius: 6px 6px 0 0; transition: height 0.3s;" title="${pt.dateLabel}: ${formatPrice(pt.fare)} (${pt.trendLabel})"></div>
            <div style="font-size: 0.68rem; color: var(--text-muted); margin-top: 6px; white-space: nowrap;">${pt.dateLabel}</div>
            <div style="font-size: 0.62rem; font-weight: 700; color: ${barBg};">${pt.trendLabel}</div>
          `;
          barsContainer.appendChild(barWrapper);
        });
      }

      if (demandBadge) {
        demandBadge.textContent = 'HIGH DEMAND (85/100)';
        demandBadge.style.color = '#f59e0b';
      }
      if (adviceText) {
        adviceText.textContent = 'Fares Likely to Rise 15%';
        adviceText.style.color = '#ef4444';
      }
    } catch (err) {
      if (barsContainer) {
        barsContainer.innerHTML = `<div style="text-align:center;width:100%;color:#ef4444;">Error: ${err.message}</div>`;
      }
    }
  };

  if (btnOpenPriceTrends) {
    btnOpenPriceTrends.addEventListener('click', () => {
      const orig = originInput ? originInput.value.split('(')[1]?.replace(')', '') || 'DEL' : 'DEL';
      const dest = destInput ? destInput.value.split('(')[1]?.replace(')', '') || 'BOM' : 'BOM';
      window.openPriceTrendsForRoute(orig, dest);
    });
  }

  if (pricingModalCloseBtn) {
    pricingModalCloseBtn.addEventListener('click', () => {
      if (pricingTrendModal) pricingTrendModal.classList.remove('open');
    });
  }

  if (btnFreezeFareNow) {
    btnFreezeFareNow.addEventListener('click', async () => {
      const orig = originInput ? originInput.value.split('(')[1]?.replace(')', '') || 'DEL' : 'DEL';
      const dest = destInput ? destInput.value.split('(')[1]?.replace(')', '') || 'BOM' : 'BOM';
      const curPrice = activePricingHistory && activePricingHistory.length > 0 ? activePricingHistory[3].fare : 150;

      btnFreezeFareNow.disabled = true;
      btnFreezeFareNow.textContent = 'Locking Fare Guarantee...';

      try {
        const res = await fetch('/api/pricing/freeze', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            origin: orig,
            destination: dest,
            currentPrice: curPrice,
            travelDate: departureDateInput ? departureDateInput.value : '2026-11-01'
          })
        });

        if (!res.ok) throw new Error('Failed to freeze fare');
        const data = await res.json();
        activeFreezeQuote = data;

        const freezeSuccessBox = document.getElementById('freezeSuccessBox');
        const freezeTokenText = document.getElementById('freezeTokenText');
        const freezeExpiryText = document.getElementById('freezeExpiryText');

        if (freezeTokenText) freezeTokenText.textContent = data.freezeReference || 'PF-78219';
        if (freezeExpiryText) freezeExpiryText.textContent = data.expirationTime || '48 hours from now';
        if (freezeSuccessBox) freezeSuccessBox.style.display = 'block';

        showToast(`Fare frozen at ${formatPrice(curPrice)}! Token: ${data.freezeReference}`);
      } catch (err) {
        showToast(`Could not freeze fare: ${err.message}`);
      } finally {
        btnFreezeFareNow.disabled = false;
        btnFreezeFareNow.textContent = '🔒 Freeze Fare for 48 Hours';
      }
    });
  }

  // ==========================================
  // MODULE 3: Cancellations and Policy Refunds
  // ==========================================
  const cancellationModal = document.getElementById('cancellationModal');
  const cancellationModalCloseBtn = document.getElementById('cancellationModalCloseBtn');
  const btnCancelRefundModal = document.getElementById('btnCancelRefundModal');
  const cancellationReasonSelect = document.getElementById('cancellationReasonSelect');
  const btnConfirmCancelRefund = document.getElementById('btnConfirmCancelRefund');

  let activeCancellationBooking = {
    reference: 'VX-89210',
    fare: 450,
    departureHours: 120 // ~5 days prior
  };

  window.openCancellationModal = function(bookingRef = 'VX-89210', originalFare = 450) {
    activeCancellationBooking.reference = bookingRef;
    activeCancellationBooking.fare = originalFare;

    const sub = document.getElementById('cancellationSubtitle');
    if (sub) sub.textContent = `Booking Ref: ${bookingRef} • Original Paid: ${formatPrice(originalFare)}`;

    const refundSuccessBox = document.getElementById('refundSuccessBox');
    if (refundSuccessBox) refundSuccessBox.style.display = 'none';

    updateRefundBreakdownDisplay();
    if (cancellationModal) cancellationModal.classList.add('open');
  };

  function updateRefundBreakdownDisplay() {
    const reason = cancellationReasonSelect ? cancellationReasonSelect.value : '';
    const isMedical = reason.includes('Medical');
    const fare = activeCancellationBooking.fare;

    let refundPct = 0.70; // 48h to 7 days
    let tierText = 'Tier 2 (48h – 7 days prior)';
    if (isMedical) {
      refundPct = 1.0;
      tierText = 'Medical Emergency Waiver (100% Full Refund)';
    }

    const netRefund = Math.round(fare * refundPct);
    const fee = fare - netRefund;

    const origEl = document.getElementById('calcOriginalFare');
    const tierEl = document.getElementById('calcPolicyTier');
    const feeEl = document.getElementById('calcCancellationFee');
    const netEl = document.getElementById('calcNetRefund');

    if (origEl) origEl.textContent = formatPrice(fare);
    if (tierEl) tierEl.textContent = tierText;
    if (feeEl) feeEl.textContent = fee === 0 ? '$0' : `-${formatPrice(fee)}`;
    if (netEl) netEl.textContent = formatPrice(netRefund);
  }

  if (cancellationReasonSelect) {
    cancellationReasonSelect.addEventListener('change', updateRefundBreakdownDisplay);
  }

  if (cancellationModalCloseBtn) {
    cancellationModalCloseBtn.addEventListener('click', () => {
      if (cancellationModal) cancellationModal.classList.remove('open');
    });
  }
  if (btnCancelRefundModal) {
    btnCancelRefundModal.addEventListener('click', () => {
      if (cancellationModal) cancellationModal.classList.remove('open');
    });
  }

  if (btnConfirmCancelRefund) {
    btnConfirmCancelRefund.addEventListener('click', async () => {
      const reason = cancellationReasonSelect ? cancellationReasonSelect.value : 'Change of plans';
      const isMedical = reason.includes('Medical');

      btnConfirmCancelRefund.disabled = true;
      btnConfirmCancelRefund.textContent = 'Processing Policy Calculation...';

      try {
        const res = await fetch('/api/cancellations/request', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            bookingReference: activeCancellationBooking.reference,
            cancellationReason: reason,
            originalFare: activeCancellationBooking.fare,
            hoursBeforeDeparture: activeCancellationBooking.departureHours,
            medicalEmergency: isMedical
          })
        });

        if (!res.ok) throw new Error('Cancellation request declined');
        const data = await res.json();

        const refundSuccessBox = document.getElementById('refundSuccessBox');
        const refundRefText = document.getElementById('refundRefText');
        const refundApprovedAmount = document.getElementById('refundApprovedAmount');

        if (refundRefText) refundRefText.textContent = data.refundReference || 'RF-99120';
        if (refundApprovedAmount) refundApprovedAmount.textContent = formatPrice(data.netRefundAmount || 405);
        if (refundSuccessBox) refundSuccessBox.style.display = 'block';

        showToast(`Cancellation approved! Refund Ref: ${data.refundReference}`);
      } catch (err) {
        showToast(`Cancellation failed: ${err.message}`);
      } finally {
        btnConfirmCancelRefund.disabled = false;
        btnConfirmCancelRefund.textContent = 'Confirm Cancellation & Refund';
      }
    });
  }

  // ==========================================
  // MODULE 4: Seat Map & Hotel Room Selection
  // ==========================================
  // A. Seat Map
  const seatMapModal = document.getElementById('seatMapModal');
  const seatModalCloseBtn = document.getElementById('seatModalCloseBtn');
  const btnCancelSeatMap = document.getElementById('btnCancelSeatMap');
  const btnConfirmSeatSelection = document.getElementById('btnConfirmSeatSelection');

  let activeSeatMapFlight = 'AI-101';
  let tempSelectedSeat = null;

  window.openSeatMapForFlight = async function(flightNumber = 'AI-101', aircraft = 'Boeing 787-8') {
    activeSeatMapFlight = flightNumber;
    tempSelectedSeat = null;

    const modalTitle = document.getElementById('seatModalTitle');
    const modalSubtitle = document.getElementById('seatModalSubtitle');
    const seatGrid = document.getElementById('seatMapGrid');
    const seatCodeEl = document.getElementById('seatMapSelectedCode');
    const seatTierEl = document.getElementById('seatMapSelectedTier');
    const seatPriceEl = document.getElementById('seatMapPrice');

    if (modalTitle) modalTitle.textContent = `Aircraft Cabin Seat Selection: ${flightNumber}`;
    if (modalSubtitle) modalSubtitle.textContent = `${aircraft} • 6x6 Cabin Configuration (A-F with aisle)`;
    if (seatCodeEl) seatCodeEl.textContent = 'None';
    if (seatTierEl) seatTierEl.textContent = '';
    if (seatPriceEl) seatPriceEl.textContent = formatPrice(0);

    if (seatGrid) seatGrid.innerHTML = '<div style="text-align:center;padding:1.5rem;color:var(--text-muted);">Generating interactive cabin seat map...</div>';

    if (seatMapModal) seatMapModal.classList.add('open');

    try {
      const res = await fetch(`/api/seats/map?aircraft=${encodeURIComponent(aircraft)}`);
      if (!res.ok) throw new Error('Failed to load seat map');
      const data = await res.json();
      renderSeatMapGrid(data);
    } catch (err) {
      if (seatGrid) seatGrid.innerHTML = `<div style="text-align:center;color:#ef4444;">${err.message}</div>`;
    }
  };

  function renderSeatMapGrid(seatMap) {
    const seatGrid = document.getElementById('seatMapGrid');
    if (!seatGrid || !seatMap || !seatMap.seats) return;

    seatGrid.innerHTML = '';
    const seatCodeEl = document.getElementById('seatMapSelectedCode');
    const seatTierEl = document.getElementById('seatMapSelectedTier');
    const seatPriceEl = document.getElementById('seatMapPrice');

    // Group seats by row (1 to 6)
    for (let row = 1; row <= seatMap.totalRows; row++) {
      const rowDiv = document.createElement('div');
      rowDiv.className = 'seat-row';
      rowDiv.style.display = 'flex';
      rowDiv.style.alignItems = 'center';
      rowDiv.style.justifyContent = 'center';
      rowDiv.style.gap = '8px';
      rowDiv.style.marginBottom = '8px';

      const rowLabel = document.createElement('span');
      rowLabel.style.fontSize = '0.75rem';
      rowLabel.style.fontWeight = '700';
      rowLabel.style.color = 'var(--text-muted)';
      rowLabel.style.width = '20px';
      rowLabel.textContent = row;
      rowDiv.appendChild(rowLabel);

      const columns = ['A', 'B', 'C', 'AISLE', 'D', 'E', 'F'];

      columns.forEach(col => {
        if (col === 'AISLE') {
          const aisle = document.createElement('div');
          aisle.style.width = '20px';
          aisle.style.textAlign = 'center';
          aisle.style.fontSize = '0.65rem';
          aisle.style.color = '#cbd5e1';
          aisle.textContent = '║';
          rowDiv.appendChild(aisle);
          return;
        }

        const seatCode = `${row}${col}`;
        const seatData = seatMap.seats.find(s => s.seatCode === seatCode) || {
          seatCode: seatCode,
          tier: row <= 2 ? 'Exit Row' : (row <= 4 ? 'Preferred' : 'Standard'),
          price: row <= 2 ? 45 : (row <= 4 ? 25 : 0),
          available: (row !== 2 || col !== 'B') && (row !== 5 || col !== 'E')
        };

        const seatBtn = document.createElement('button');
        seatBtn.type = 'button';
        seatBtn.className = 'cabin-seat';
        seatBtn.setAttribute('data-seat', seatCode);
        seatBtn.setAttribute('data-tier', seatData.tier);
        seatBtn.setAttribute('data-price', seatData.price);

        let bg = '#ffffff';
        let border = '#cbd5e1';
        let color = '#334155';

        if (!seatData.available) {
          bg = '#e2e8f0';
          border = '#cbd5e1';
          color = '#94a3b8';
          seatBtn.disabled = true;
          seatBtn.title = `${seatCode}: Occupied`;
        } else if (seatData.tier === 'Exit Row') {
          bg = '#fef3c7';
          border = '#f59e0b';
          color = '#92400e';
          seatBtn.title = `${seatCode}: Extra Legroom ($45)`;
        } else if (seatData.tier === 'Preferred') {
          bg = '#eff6ff';
          border = '#3b82f6';
          color = '#1e40af';
          seatBtn.title = `${seatCode}: Preferred Seat ($25)`;
        } else {
          seatBtn.title = `${seatCode}: Standard ($0)`;
        }

        seatBtn.style.width = '34px';
        seatBtn.style.height = '34px';
        seatBtn.style.borderRadius = '6px';
        seatBtn.style.border = `2px solid ${border}`;
        seatBtn.style.background = bg;
        seatBtn.style.color = color;
        seatBtn.style.fontSize = '0.72rem';
        seatBtn.style.fontWeight = '700';
        seatBtn.style.cursor = seatData.available ? 'pointer' : 'not-allowed';
        seatBtn.textContent = col;

        if (seatData.available) {
          seatBtn.addEventListener('click', () => {
            document.querySelectorAll('.cabin-seat.selected').forEach(s => {
              s.classList.remove('selected');
              s.style.boxShadow = 'none';
            });
            seatBtn.classList.add('selected');
            seatBtn.style.boxShadow = '0 0 0 3px #10b981';

            tempSelectedSeat = {
              seatCode: seatCode,
              tier: seatData.tier,
              price: seatData.price
            };

            if (seatCodeEl) seatCodeEl.textContent = seatCode;
            if (seatTierEl) seatTierEl.textContent = `(${seatData.tier})`;
            if (seatPriceEl) seatPriceEl.textContent = formatPrice(seatData.price);
          });
        }

        rowDiv.appendChild(seatBtn);
      });

      seatGrid.appendChild(rowDiv);
    }
  }

  if (seatModalCloseBtn) {
    seatModalCloseBtn.addEventListener('click', () => {
      if (seatMapModal) seatMapModal.classList.remove('open');
    });
  }
  if (btnCancelSeatMap) {
    btnCancelSeatMap.addEventListener('click', () => {
      if (seatMapModal) seatMapModal.classList.remove('open');
    });
  }

  if (btnConfirmSeatSelection) {
    btnConfirmSeatSelection.addEventListener('click', async () => {
      if (!tempSelectedSeat) {
        showToast('Please tap on an available seat to select it.');
        return;
      }

      currentSelectedSeat = tempSelectedSeat;

      try {
        await fetch('/api/seats/select', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            flightNumber: activeSeatMapFlight,
            seatCode: currentSelectedSeat.seatCode,
            passengerName: currentUser.name,
            tier: currentSelectedSeat.tier
          })
        });
      } catch {
        // demo resilience
      }

      const badge = document.getElementById('bookingSelectedSeatBadge');
      const val = document.getElementById('bookingSelectedSeatVal');
      const tier = document.getElementById('bookingSelectedSeatTier');
      if (badge) badge.style.display = 'block';
      if (val) val.textContent = currentSelectedSeat.seatCode;
      if (tier) tier.textContent = currentSelectedSeat.tier;

      if (seatMapModal) seatMapModal.classList.remove('open');
      showToast(`Seat ${currentSelectedSeat.seatCode} (${currentSelectedSeat.tier}) assigned!`);
    });
  }

  // Forward legacy openHotelModal to expanded hotel reservation module
  window.openHotelModal = function(hotelId = 'hotel-1') {
    if (typeof window.openHotelReservation === 'function') {
      window.openHotelReservation(hotelId);
    }
  };

  // ==========================================
  // MODULE 5: Customer Reviews and Ratings
  // ==========================================
  const reviewRatingFilter = document.getElementById('reviewRatingFilter');
  const btnOpenWriteReview = document.getElementById('btnOpenWriteReview');
  const reviewModal = document.getElementById('reviewModal');
  const reviewModalCloseBtn = document.getElementById('reviewModalCloseBtn');
  const btnCancelReviewModal = document.getElementById('btnCancelReviewModal');
  const btnSubmitReviewAction = document.getElementById('btnSubmitReviewAction');
  const starPickers = document.querySelectorAll('.star-pick');
  const reviewStarRatingVal = document.getElementById('reviewStarRatingVal');
  const reviewPhotoInput = document.getElementById('reviewPhotoInput');
  const photoValidationMsg = document.getElementById('photoValidationMsg');

  async function loadReviews(rating = 'ALL', sortBy = 'NEWEST') {
    const list = document.getElementById('reviewsListGrid');
    if (!list) return;

    list.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">Loading community reviews...</div>';

    try {
      const res = await fetch(`/api/reviews?itemType=ALL&rating=${encodeURIComponent(rating)}&sortBy=${encodeURIComponent(sortBy)}`);
      if (!res.ok) throw new Error('Could not load reviews');
      const data = await res.json();
      renderReviews(data);
    } catch (err) {
      list.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: #ef4444;">${err.message}</div>`;
    }
  }

  function renderReviews(reviews) {
    const list = document.getElementById('reviewsListGrid');
    if (!list) return;

    list.innerHTML = '';
    if (!reviews || reviews.length === 0) {
      list.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">No reviews found matching this filter.</div>';
      return;
    }

    reviews.forEach(r => {
      const starsStr = '★'.repeat(r.rating || 5) + '☆'.repeat(5 - (r.rating || 5));
      const hostReplyHtml = r.hostReply
        ? `<div class="review-host-reply" style="background:#f8fafc; border-left:3px solid var(--primary); padding:0.6rem 0.85rem; border-radius:0 8px 8px 0; margin-top:0.75rem; font-size:0.8rem; color:#475569;">
            <strong>Official Response:</strong> ${r.hostReply}
           </div>`
        : '';

      const photoHtml = r.photoUrl
        ? `<div style="margin-top:0.6rem;"><img src="${r.photoUrl}" alt="Review photo" style="width:100%; max-height:160px; object-fit:cover; border-radius:8px;" loading="lazy"></div>`
        : '';

      const card = document.createElement('div');
      card.className = 'review-card';
      card.style.background = '#ffffff';
      card.style.border = '1px solid var(--border)';
      card.style.borderRadius = '12px';
      card.style.padding = '1.25rem';
      card.style.display = 'flex';
      card.style.flexDirection = 'column';
      card.style.justifyContent = 'space-between';

      card.innerHTML = `
        <div>
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
            <div>
              <strong style="color: var(--secondary); font-size: 0.95rem;">${r.reviewerName}</strong>
              <span style="font-size: 0.75rem; color: #10b981; margin-left: 4px;">✓ Verified Traveler</span>
              <div style="font-size: 0.78rem; color: var(--text-muted);">${r.itemName} &bull; ${r.createdDate || 'Recent'}</div>
            </div>
            <div style="color: #f59e0b; font-size: 1rem;">${starsStr}</div>
          </div>

          <h5 style="margin: 0.4rem 0; font-size: 0.95rem; color: var(--secondary);">${r.title}</h5>
          <p style="font-size: 0.85rem; color: #475569; line-height: 1.5; margin: 0;">"${r.comments}"</p>
          ${photoHtml}
          ${hostReplyHtml}
        </div>

        <div style="margin-top: 1rem; border-top: 1px solid var(--border); padding-top: 0.6rem; display: flex; justify-content: space-between; align-items: center; font-size: 0.78rem;">
          <button type="button" onclick="this.textContent='✓ Helpful (1)';" style="background:none; border:none; color:var(--text-muted); cursor:pointer;">👍 Helpful</button>
          <button type="button" onclick="window.reportReview('${r.id}');" style="background:none; border:none; color:#94a3b8; cursor:pointer;">Report</button>
        </div>
      `;

      list.appendChild(card);
    });
  }

  window.reportReview = async function(reviewId) {
    try {
      await fetch(`/api/reviews/${encodeURIComponent(reviewId)}/report`, { method: 'POST' });
      showToast('Thank you. Review flagged for content moderation review.');
    } catch {
      showToast('Review flagged for moderation.');
    }
  };

  if (reviewRatingFilter) {
    reviewRatingFilter.addEventListener('change', (e) => {
      loadReviews(e.target.value, 'NEWEST');
    });
  }

  if (btnOpenWriteReview) {
    btnOpenWriteReview.addEventListener('click', () => {
      if (reviewModal) reviewModal.classList.add('open');
    });
  }
  if (reviewModalCloseBtn) {
    reviewModalCloseBtn.addEventListener('click', () => {
      if (reviewModal) reviewModal.classList.remove('open');
    });
  }
  if (btnCancelReviewModal) {
    btnCancelReviewModal.addEventListener('click', () => {
      if (reviewModal) reviewModal.classList.remove('open');
    });
  }

  // Star Rating Interactive Picker
  starPickers.forEach(star => {
    star.addEventListener('click', () => {
      const val = parseInt(star.getAttribute('data-val'), 10);
      if (reviewStarRatingVal) reviewStarRatingVal.value = val;
      starPickers.forEach(s => {
        const sVal = parseInt(s.getAttribute('data-val'), 10);
        if (sVal <= val) {
          s.style.color = '#f59e0b';
        } else {
          s.style.color = '#cbd5e1';
        }
      });
    });
  });

  // Photo Validation
  if (reviewPhotoInput) {
    reviewPhotoInput.addEventListener('change', () => {
      const file = reviewPhotoInput.files[0];
      if (!file) return;

      const allowed = ['image/jpeg', 'image/png', 'image/webp'];
      if (!allowed.includes(file.type)) {
        if (photoValidationMsg) {
          photoValidationMsg.textContent = '❌ Invalid format. Please attach JPEG, PNG, or WebP only.';
          photoValidationMsg.style.color = '#ef4444';
        }
        reviewPhotoInput.value = '';
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        if (photoValidationMsg) {
          photoValidationMsg.textContent = '❌ File too large. Image must be under 5MB.';
          photoValidationMsg.style.color = '#ef4444';
        }
        reviewPhotoInput.value = '';
        return;
      }

      if (photoValidationMsg) {
        photoValidationMsg.textContent = `✓ Image selected (${(file.size / 1024).toFixed(0)} KB). Ready to publish.`;
        photoValidationMsg.style.color = '#10b981';
      }
    });
  }

  if (btnSubmitReviewAction) {
    btnSubmitReviewAction.addEventListener('click', async () => {
      const author = document.getElementById('reviewAuthorName')?.value.trim() || currentUser.name;
      const item = document.getElementById('reviewItemName')?.value.trim() || 'VoyageX Travel';
      const title = document.getElementById('reviewTitleInput')?.value.trim();
      const comments = document.getElementById('reviewCommentsInput')?.value.trim();
      const rating = parseInt(reviewStarRatingVal ? reviewStarRatingVal.value : '5', 10);

      if (!title || !comments) {
        showToast('Please provide a title and detailed comments.');
        return;
      }

      btnSubmitReviewAction.disabled = true;
      btnSubmitReviewAction.textContent = 'Submitting...';

      try {
        const res = await fetch('/api/reviews', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            reviewerName: author,
            itemName: item,
            itemType: 'EXPERIENCE',
            rating: rating,
            cleanlinessRating: rating,
            serviceRating: rating,
            valueRating: rating,
            title: title,
            comments: comments,
            photoUrl: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80'
          })
        });

        if (!res.ok) throw new Error('Submission failed');
        if (reviewModal) reviewModal.classList.remove('open');
        showToast('Thank you! Your verified review has been published.');
        loadReviews('ALL', 'NEWEST');
      } catch (err) {
        showToast(`Could not post review: ${err.message}`);
      } finally {
        btnSubmitReviewAction.disabled = false;
        btnSubmitReviewAction.textContent = 'Publish Review';
      }
    });
  }

  // ==========================================
  // MODULE 6: Personalized Recommendations
  // ==========================================
  let selectedStyle = 'Romantic';
  let selectedVibe = 'Beach';

  async function loadRecommendations(style = 'Romantic', vibe = 'Beach') {
    const grid = document.getElementById('recommendationsGrid');
    if (!grid) return;

    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">Finding top destination matches...</div>';

    try {
      const res = await fetch('/api/recommendations/match', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          travelStyle: style,
          settingVibe: vibe,
          travelerCount: currentSearchParams.passengers || 2
        })
      });

      if (!res.ok) throw new Error('Recommendation matching failed');
      const data = await res.json();
      renderRecommendations(data);
    } catch (err) {
      grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: #ef4444;">${err.message}</div>`;
    }
  }

  function renderRecommendations(items) {
    const grid = document.getElementById('recommendationsGrid');
    if (!grid) return;

    grid.innerHTML = '';
    if (!items || items.length === 0) {
      grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">No recommendations found for this style.</div>';
      return;
    }

    items.forEach(item => {
      const card = document.createElement('article');
      card.className = 'destination-card';
      card.innerHTML = `
        <div class="destination-image-wrapper">
          <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=800&q=80'}" alt="${item.destinationName}" class="destination-image" loading="lazy">
          <span class="card-badge" style="background:#10b981; color:#fff;">${item.matchScore}% Match</span>
        </div>
        <div class="destination-content">
          <div class="destination-location">
            <h3 class="destination-city">${item.destinationName}</h3>
            <span class="destination-country">${item.country}</span>
          </div>
          <div style="background: #f8fafc; border-left: 3px solid var(--primary); padding: 0.5rem 0.75rem; border-radius: 0 6px 6px 0; margin: 0.6rem 0; font-size: 0.8rem; color: #475569;">
            <strong>Why Recommended:</strong> ${item.reason}
          </div>
          <div class="destination-footer">
            <div class="destination-price-box">
              <span class="price-sub">Starting from</span>
              <div class="price-amount">${formatPrice(item.startingPrice)}</div>
            </div>
            <button type="button" class="btn-book-sm" onclick="window.exploreDestination('${item.destinationName}')">Explore Flights</button>
          </div>
          <div style="display:flex; justify-content:space-between; margin-top:0.75rem; font-size:0.75rem; color:var(--text-muted); border-top:1px dashed var(--border); padding-top:0.4rem;">
            <span>Was this match helpful?</span>
            <div>
              <button type="button" onclick="this.textContent='✓ Helpful';showToast('Thanks for your feedback!');" style="background:none;border:none;cursor:pointer;color:var(--primary);margin-right:8px;">👍 Yes</button>
              <button type="button" onclick="this.textContent='Noted';showToast('We will fine-tune your matches.');" style="background:none;border:none;cursor:pointer;color:var(--text-muted);">👎 No</button>
            </div>
          </div>
        </div>
      `;
      grid.appendChild(card);
    });
  }

  // Quiz Pills Listeners
  document.querySelectorAll('.quiz-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      const type = pill.getAttribute('data-type');
      const val = pill.getAttribute('data-val');

      if (type === 'style') {
        selectedStyle = val;
        document.querySelectorAll('#stylePillsGroup .quiz-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
      } else if (type === 'vibe') {
        selectedVibe = val;
        document.querySelectorAll('#vibePillsGroup .quiz-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
      }

      loadRecommendations(selectedStyle, selectedVibe);
    });
  });

  // ==========================================
  // MODULE 7: Newsletter, Auth & My Trips
  // ==========================================
  // Newsletter
  window.handleNewsletterSubmit = async function() {
    const input = document.getElementById('newsletterEmail');
    const successBox = document.getElementById('newsletterSuccessBox');
    const btn = document.getElementById('newsletterSubmitBtn');

    if (!input) return;
    const email = input.value.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      showToast('Please enter a valid email address.');
      input.focus();
      return;
    }

    if (btn) {
      btn.disabled = true;
      btn.textContent = 'Subscribing...';
    }

    try {
      const res = await fetch('/api/newsletter/subscribe', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email })
      });
      if (!res.ok) throw new Error('Subscription error');

      if (successBox) successBox.style.display = 'block';
      input.disabled = true;
      showToast('Subscribed! Welcome coupon WELCOME15 unlocked.');
    } catch {
      if (successBox) successBox.style.display = 'block';
      showToast('Subscribed! Welcome coupon WELCOME15 unlocked.');
    } finally {
      if (btn) btn.textContent = 'Subscribed ✓';
    }
  };

  // ============================================================
  // MODULE 7: Comprehensive Auth, User State & Profile Nav
  // ============================================================
  const authModal = document.getElementById('authModal');
  const btnAuth = document.getElementById('btnAuth');
  const authModalCloseBtn = document.getElementById('authModalCloseBtn');
  const userProfileBadge = document.getElementById('userProfileBadge');
  const navAvatarDot = document.getElementById('navAvatarDot');
  const navUserName = document.getElementById('navUserName');
  const userDropdownWrapper = document.getElementById('userDropdownWrapper');
  const userMenuDropdown = document.getElementById('userMenuDropdown');

  // Menu items
  const menuHeaderName = document.getElementById('dropdownUserName') || document.getElementById('menuHeaderName');
  const menuHeaderEmail = document.getElementById('dropdownUserEmail') || document.getElementById('menuHeaderEmail');
  const menuHeaderTier = document.getElementById('dropdownUserTier') || document.getElementById('menuHeaderTier');
  const navUserTierBadge = document.getElementById('navUserTierBadge');
  const savedDealsCount = document.getElementById('savedDealsCount');

  const menuMyTrips = document.getElementById('btnDropdownMyTrips') || document.getElementById('menuMyTrips');
  const menuSavedDeals = document.getElementById('btnDropdownSavedDeals') || document.getElementById('menuSavedDeals');
  const menuMembership = document.getElementById('btnDropdownMembership') || document.getElementById('menuMembership');
  const menuSignOut = document.getElementById('btnDropdownSignOut') || document.getElementById('menuSignOut');

  // Auth Tabs & Panels
  const tabBtnSignIn = document.getElementById('tabBtnSignIn');
  const tabBtnRegister = document.getElementById('tabBtnRegister');
  const authSignInPanel = document.getElementById('authSignInPanel');
  const authRegisterPanel = document.getElementById('authRegisterPanel');
  const authForgotPanel = document.getElementById('authForgotPanel');
  const authAlertBanner = document.getElementById('authAlertBanner');

  // Forms & Inputs
  const formSignIn = document.getElementById('formSignIn');
  const loginEmail = document.getElementById('loginEmail');
  const loginPassword = document.getElementById('loginPassword');
  const toggleLoginPwd = document.getElementById('toggleLoginPwd');
  const linkForgotPassword = document.getElementById('linkForgotPassword');
  const btnSignInSubmit = document.getElementById('btnSignInSubmit');

  const formRegister = document.getElementById('formRegister');
  const registerName = document.getElementById('registerName');
  const registerEmail = document.getElementById('registerEmail');
  const registerPassword = document.getElementById('registerPassword');
  const toggleRegisterPwd = document.getElementById('toggleRegisterPwd');
  const registerConfirmPassword = document.getElementById('registerConfirmPassword');
  const toggleRegisterConfirmPwd = document.getElementById('toggleRegisterConfirmPwd');
  const registerTierSelect = document.getElementById('registerTierSelect');
  const btnRegisterSubmit = document.getElementById('btnRegisterSubmit');

  const formForgot = document.getElementById('formForgot');
  const forgotEmail = document.getElementById('forgotEmail');
  const btnForgotSubmit = document.getElementById('btnForgotSubmit');
  const forgotSuccessBox = document.getElementById('forgotSuccessBox');
  const forgotSuccessText = document.getElementById('forgotSuccessText');
  const linkBackToSignIn = document.getElementById('linkBackToSignIn');

  // Error spans
  const loginEmailError = document.getElementById('loginEmailError');
  const loginPasswordError = document.getElementById('loginPasswordError');
  const registerNameError = document.getElementById('registerNameError');
  const registerEmailError = document.getElementById('registerEmailError');
  const registerPasswordError = document.getElementById('registerPasswordError');
  const registerConfirmPasswordError = document.getElementById('registerConfirmPasswordError');
  const forgotEmailError = document.getElementById('forgotEmailError');

  // Saved Deals Set for quick lookup
  let userSavedDealIds = new Set();

  function showAuthAlert(message, type = 'error') {
    if (!authAlertBanner) return;
    authAlertBanner.textContent = message;
    authAlertBanner.className = `auth-alert ${type}`;
    authAlertBanner.style.display = 'block';
  }

  function clearAuthAlert() {
    if (!authAlertBanner) return;
    authAlertBanner.textContent = '';
    authAlertBanner.style.display = 'none';
  }

  function switchAuthTab(targetTab) {
    clearAuthAlert();
    if (targetTab === 'signin') {
      if (tabBtnSignIn) tabBtnSignIn.classList.add('active');
      if (tabBtnRegister) tabBtnRegister.classList.remove('active');
      if (authSignInPanel) authSignInPanel.style.display = 'block';
      if (authRegisterPanel) authRegisterPanel.style.display = 'none';
      if (authForgotPanel) authForgotPanel.style.display = 'none';
      if (loginEmail) loginEmail.focus();
    } else if (targetTab === 'register') {
      if (tabBtnSignIn) tabBtnSignIn.classList.remove('active');
      if (tabBtnRegister) tabBtnRegister.classList.add('active');
      if (authSignInPanel) authSignInPanel.style.display = 'none';
      if (authRegisterPanel) authRegisterPanel.style.display = 'block';
      if (authForgotPanel) authForgotPanel.style.display = 'none';
      if (registerName) registerName.focus();
    } else if (targetTab === 'forgot') {
      if (tabBtnSignIn) tabBtnSignIn.classList.remove('active');
      if (tabBtnRegister) tabBtnRegister.classList.remove('active');
      if (authSignInPanel) authSignInPanel.style.display = 'none';
      if (authRegisterPanel) authRegisterPanel.style.display = 'none';
      if (authForgotPanel) authForgotPanel.style.display = 'block';
      if (forgotSuccessBox) forgotSuccessBox.style.display = 'none';
      if (forgotEmail) forgotEmail.focus();
    }
  }

  if (tabBtnSignIn) tabBtnSignIn.addEventListener('click', () => switchAuthTab('signin'));
  if (tabBtnRegister) tabBtnRegister.addEventListener('click', () => switchAuthTab('register'));
  if (linkForgotPassword) linkForgotPassword.addEventListener('click', () => switchAuthTab('forgot'));
  if (linkBackToSignIn) linkBackToSignIn.addEventListener('click', () => switchAuthTab('signin'));

  // Password Visibility Toggle Helpers
  function setupPasswordToggle(btn, input) {
    if (!btn || !input) return;
    btn.addEventListener('click', () => {
      const isPassword = input.type === 'password';
      input.type = isPassword ? 'text' : 'password';
      const eyeOpen = btn.querySelector('.eye-open');
      const eyeClosed = btn.querySelector('.eye-closed');
      if (eyeOpen) eyeOpen.style.display = isPassword ? 'none' : 'block';
      if (eyeClosed) eyeClosed.style.display = isPassword ? 'block' : 'none';
      btn.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
    });
  }

  setupPasswordToggle(toggleLoginPwd, loginPassword);
  setupPasswordToggle(toggleRegisterPwd, registerPassword);
  setupPasswordToggle(toggleRegisterConfirmPwd, registerConfirmPassword);

  // Update Nav Profile Display
  function updateNavUserProfile(user) {
    if (user && user.email) {
      if (btnAuth) btnAuth.style.display = 'none';
      if (userDropdownWrapper) userDropdownWrapper.style.display = 'inline-block';
      if (userProfileBadge) userProfileBadge.style.display = 'inline-flex';
      if (navAvatarDot) navAvatarDot.textContent = user.initials || 'VX';
      if (navUserName) navUserName.textContent = user.name;
      if (navUserTierBadge) navUserTierBadge.textContent = user.tier ? user.tier.replace('VoyageX ', '') : 'Plus';
      if (menuHeaderName) menuHeaderName.textContent = user.name;
      if (menuHeaderEmail) menuHeaderEmail.textContent = user.email;
      if (menuHeaderTier) menuHeaderTier.textContent = user.tier || 'VoyageX Explorer';

      // Load saved deal IDs into Set
      if (user.savedDealIds && Array.isArray(user.savedDealIds)) {
        userSavedDealIds = new Set(user.savedDealIds);
      }
      if (savedDealsCount) savedDealsCount.textContent = userSavedDealIds.size;
      localStorage.setItem('voyagex_active_user', JSON.stringify(user));
    } else {
      if (btnAuth) btnAuth.style.display = 'inline-flex';
      if (userDropdownWrapper) userDropdownWrapper.style.display = 'none';
      if (userProfileBadge) userProfileBadge.style.display = 'none';
      if (userMenuDropdown) userMenuDropdown.classList.remove('show');
      userSavedDealIds.clear();
      if (savedDealsCount) savedDealsCount.textContent = '0';
      localStorage.removeItem('voyagex_active_user');
    }
  }

  // Restore session on load — read from auth guard-written keys
  // By the time DOMContentLoaded fires, the auth guard IIFE has already validated the token.
  const sessionUserJson = sessionStorage.getItem('vx_session_user')
    || localStorage.getItem('vx_session_user')
    || localStorage.getItem('voyagex_active_user');
  if (sessionUserJson) {
    try {
      const parsed = JSON.parse(sessionUserJson);
      if (parsed && parsed.email) {
        currentUser = parsed;
        updateNavUserProfile(currentUser);
      }
    } catch {
      /* ignore malformed JSON */
    }
  }

  // Dropdown toggle & outside click handling
  if (userProfileBadge) {
    userProfileBadge.addEventListener('click', (e) => {
      e.stopPropagation();
      if (userMenuDropdown) userMenuDropdown.classList.toggle('show');
    });
  }

  document.addEventListener('click', (e) => {
    if (userMenuDropdown && !userMenuDropdown.contains(e.target) && e.target !== userProfileBadge) {
      userMenuDropdown.classList.remove('show');
    }
  });

  // Modal open/close preserving search details
  if (btnAuth) {
    btnAuth.addEventListener('click', () => {
      switchAuthTab('signin');
      if (authModal) authModal.classList.add('open');
    });
  }

  function closeAuthModal() {
    if (authModal) authModal.classList.remove('open');
    clearAuthAlert();
  }

  if (authModalCloseBtn) authModalCloseBtn.addEventListener('click', closeAuthModal);
  if (authModal) {
    authModal.addEventListener('click', (e) => {
      if (e.target === authModal) closeAuthModal();
    });
  }

  // --- Sign In Submission ---
  if (formSignIn) {
    formSignIn.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAuthAlert();
      const email = loginEmail ? loginEmail.value.trim() : '';
      const password = loginPassword ? loginPassword.value : '';

      let hasError = false;
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!email || !emailRegex.test(email)) {
        if (loginEmailError) {
          loginEmailError.textContent = 'Please enter a valid email address.';
          loginEmailError.classList.add('show');
        }
        hasError = true;
      } else if (loginEmailError) {
        loginEmailError.classList.remove('show');
      }

      if (!password) {
        if (loginPasswordError) {
          loginPasswordError.textContent = 'Password is required.';
          loginPasswordError.classList.add('show');
        }
        hasError = true;
      } else if (loginPasswordError) {
        loginPasswordError.classList.remove('show');
      }

      if (hasError) return;

      if (btnSignInSubmit) {
        btnSignInSubmit.disabled = true;
        btnSignInSubmit.textContent = 'Signing in...';
      }

      try {
        const res = await fetch('/api/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, password })
        });
        const data = await res.json();

        if (!res.ok) {
          showAuthAlert(data.message || 'Invalid email or password.');
          return;
        }

        currentUser = {
          id: data.user.id,
          name: data.user.name,
          email: data.user.email,
          initials: data.user.initials,
          tier: data.user.tier,
          savedDealIds: data.user.savedDealIds || []
        };
        updateNavUserProfile(currentUser);
        closeAuthModal();
        showToast(`Welcome back, ${currentUser.name}!`);
        // Refresh deals to update heart buttons
        loadVipDeals(currentDealCategory);
      } catch (err) {
        showAuthAlert('Unable to connect to authentication service. Please try again.');
      } finally {
        if (btnSignInSubmit) {
          btnSignInSubmit.disabled = false;
          btnSignInSubmit.textContent = 'Sign In';
        }
      }
    });
  }

  // --- Registration Submission ---
  if (formRegister) {
    formRegister.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAuthAlert();
      const name = registerName ? registerName.value.trim() : '';
      const email = registerEmail ? registerEmail.value.trim() : '';
      const password = registerPassword ? registerPassword.value : '';
      const confirmPassword = registerConfirmPassword ? registerConfirmPassword.value : '';
      const tier = registerTierSelect ? registerTierSelect.value : 'VoyageX Explorer';

      let hasError = false;
      if (!name) {
        if (registerNameError) {
          registerNameError.textContent = 'Full name is required.';
          registerNameError.classList.add('show');
        }
        hasError = true;
      } else if (registerNameError) {
        registerNameError.classList.remove('show');
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!email || !emailRegex.test(email)) {
        if (registerEmailError) {
          registerEmailError.textContent = 'Please enter a valid email address.';
          registerEmailError.classList.add('show');
        }
        hasError = true;
      } else if (registerEmailError) {
        registerEmailError.classList.remove('show');
      }

      if (!password || password.length < 6) {
        if (registerPasswordError) {
          registerPasswordError.textContent = 'Password must be at least 6 characters.';
          registerPasswordError.classList.add('show');
        }
        hasError = true;
      } else if (registerPasswordError) {
        registerPasswordError.classList.remove('show');
      }

      if (password !== confirmPassword) {
        if (registerConfirmPasswordError) {
          registerConfirmPasswordError.textContent = 'Passwords do not match.';
          registerConfirmPasswordError.classList.add('show');
        }
        hasError = true;
      } else if (registerConfirmPasswordError) {
        registerConfirmPasswordError.classList.remove('show');
      }

      if (hasError) return;

      if (btnRegisterSubmit) {
        btnRegisterSubmit.disabled = true;
        btnRegisterSubmit.textContent = 'Creating Account...';
      }

      try {
        const res = await fetch('/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name, email, password, confirmPassword, tier })
        });
        const data = await res.json();

        if (!res.ok) {
          showAuthAlert(data.message || 'Registration failed. Email may already be in use.');
          return;
        }

        currentUser = {
          id: data.user.id,
          name: data.user.name,
          email: data.user.email,
          initials: data.user.initials,
          tier: data.user.tier,
          savedDealIds: data.user.savedDealIds || []
        };
        updateNavUserProfile(currentUser);
        closeAuthModal();
        showToast(`Account created! Welcome to VoyageX, ${currentUser.name}.`);
      } catch (err) {
        showAuthAlert('Unable to complete registration. Please try again.');
      } finally {
        if (btnRegisterSubmit) {
          btnRegisterSubmit.disabled = false;
          btnRegisterSubmit.textContent = 'Create VoyageX Account';
        }
      }
    });
  }

  // --- Forgot Password Flow ---
  if (formForgot) {
    formForgot.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAuthAlert();
      const email = forgotEmail ? forgotEmail.value.trim() : '';

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!email || !emailRegex.test(email)) {
        if (forgotEmailError) {
          forgotEmailError.textContent = 'Please enter a valid email address.';
          forgotEmailError.classList.add('show');
        }
        return;
      } else if (forgotEmailError) {
        forgotEmailError.classList.remove('show');
      }

      if (btnForgotSubmit) {
        btnForgotSubmit.disabled = true;
        btnForgotSubmit.textContent = 'Sending...';
      }

      try {
        const res = await fetch('/api/auth/forgot-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email })
        });
        const data = await res.json();

        if (forgotSuccessBox) {
          forgotSuccessBox.style.display = 'block';
          if (forgotSuccessText) {
            forgotSuccessText.textContent = data.message || `Password reset link simulated for ${email}. Check your inbox.`;
          }
        }
        formForgot.reset();
      } catch (err) {
        showAuthAlert('Unable to process password reset request.');
      } finally {
        if (btnForgotSubmit) {
          btnForgotSubmit.disabled = false;
          btnForgotSubmit.textContent = 'Send Password Reset Link';
        }
      }
    });
  }

  // Demo user quick login
  window.selectDemoUser = async function(userId) {
    try {
      const res = await fetch('/api/auth/demo-login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: userId })
      });
      if (res.ok) {
        const data = await res.json();
        currentUser = {
          id: data.user ? data.user.id : userId,
          name: data.user ? data.user.name : (userId === 'elena' ? 'Elena Rostova' : 'Arun Patel'),
          email: data.user ? data.user.email : (userId === 'elena' ? 'elena.rostova@example.com' : 'arun.patel@example.com'),
          initials: data.user ? data.user.initials : (userId === 'elena' ? 'ER' : 'AP'),
          tier: data.user ? data.user.tier : 'Gold Member',
          savedDealIds: data.user ? (data.user.savedDealIds || []) : []
        };
        updateNavUserProfile(currentUser);
        closeAuthModal();
        showToast(`Signed in as ${currentUser.name} (${currentUser.tier})`);
        loadVipDeals(currentDealCategory);
      }
    } catch (err) {
      console.warn('Demo login error:', err);
    }
  };

  // Sign Out Handler — revokes session server-side and redirects to login
  if (menuSignOut) {
    menuSignOut.addEventListener('click', async () => {
      const SESSION_KEY = 'vx_session_token';
      const token = sessionStorage.getItem(SESSION_KEY) || localStorage.getItem(SESSION_KEY);
      showToast('Signing out of VoyageX...');
      try {
        if (token) {
          await fetch('/api/auth/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token })
          });
        }
      } catch (e) { /* proceed even if network fails */ }
      // Clear all auth state
      sessionStorage.clear();
      localStorage.removeItem(SESSION_KEY);
      localStorage.removeItem('vx_session_user');
      localStorage.removeItem('voyagex_active_user');
      // Redirect to login after brief delay for toast visibility
      setTimeout(() => { window.location.replace('/login'); }, 600);
    });
  }

  // ============================================================
  // MODULE 8: Expanded Hotel Recommendations Engine
  // ============================================================
  let currentHotelCategory = 'ALL';
  let cachedHotels = [];
  let currentSelectedHotel = null;

  const hotelCategoryPills = document.getElementById('hotelCategoryPills');
  const hotelLocationFilter = document.getElementById('hotelLocationFilter');
  const hotelPriceFilter = document.getElementById('hotelPriceFilter');
  const hotelPriceVal = document.getElementById('hotelPriceVal');
  const hotelRatingFilter = document.getElementById('hotelRatingFilter');
  const hotelAmenityFilter = document.getElementById('hotelAmenityFilter');
  const hotelSortFilter = document.getElementById('hotelSortFilter');
  const btnResetHotelFilters = document.getElementById('btnResetHotelFilters');
  const hotelsCardsGrid = document.getElementById('hotelsCardsGrid');

  // Hotel Details Modal Elements
  const hotelDetailsModal = document.getElementById('hotelDetailsModal');
  const hotelDetailsTitle = document.getElementById('hotelDetailsTitle');
  const hotelDetailsSubtitle = document.getElementById('hotelDetailsSubtitle');
  const hotelDetailsImage = document.getElementById('hotelDetailsImage');
  const hotelDetailsCategoryBadge = document.getElementById('hotelDetailsCategoryBadge');
  const hotelDetailsAvailabilityBadge = document.getElementById('hotelDetailsAvailabilityBadge');
  const hotelDetailsRating = document.getElementById('hotelDetailsRating');
  const hotelDetailsLocation = document.getElementById('hotelDetailsLocation');
  const hotelDetailsPrice = document.getElementById('hotelDetailsPrice');
  const hotelDetailsDesc = document.getElementById('hotelDetailsDesc');
  const hotelDetailsAmenities = document.getElementById('hotelDetailsAmenities');
  const btnHotelDetailsBack = document.getElementById('btnHotelDetailsBack');
  const btnHotelDetailsBook = document.getElementById('btnHotelDetailsBook');
  const hotelDetailsCloseBtn = document.getElementById('hotelDetailsCloseBtn');

  // Hotel Reservation Modal Elements
  const hotelModal = document.getElementById('hotelModal');
  const hotelModalTitle = document.getElementById('hotelModalTitle');
  const hotelModalSubtitle = document.getElementById('hotelModalSubtitle');
  const hotelRoomsList = document.getElementById('hotelRoomsList');
  const hotelReservationForm = document.getElementById('hotelReservationForm');
  const hotelGuestName = document.getElementById('hotelGuestName');
  const hotelCheckIn = document.getElementById('hotelCheckIn');
  const hotelNights = document.getElementById('hotelNights');
  const hotelTotalFare = document.getElementById('hotelTotalFare');
  const btnCancelHotelModal = document.getElementById('btnCancelHotelModal');
  const btnConfirmHotelStay = document.getElementById('btnConfirmHotelStay');
  const hotelModalCloseBtn = document.getElementById('hotelModalCloseBtn');

  // Category navigation pills click handler
  if (hotelCategoryPills) {
    hotelCategoryPills.querySelectorAll('.category-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        hotelCategoryPills.querySelectorAll('.category-pill').forEach(p => {
          p.classList.remove('active');
          p.setAttribute('aria-selected', 'false');
        });
        pill.classList.add('active');
        pill.setAttribute('aria-selected', 'true');
        currentHotelCategory = pill.getAttribute('data-cat') || 'ALL';
        loadHotels();
      });
    });
  }

  // Filter toolbar event listeners
  if (hotelLocationFilter) hotelLocationFilter.addEventListener('change', loadHotels);
  if (hotelRatingFilter) hotelRatingFilter.addEventListener('change', loadHotels);
  if (hotelAmenityFilter) hotelAmenityFilter.addEventListener('change', loadHotels);
  if (hotelSortFilter) hotelSortFilter.addEventListener('change', loadHotels);
  if (hotelPriceFilter) {
    hotelPriceFilter.addEventListener('input', (e) => {
      if (hotelPriceVal) hotelPriceVal.textContent = `$${e.target.value}`;
    });
    hotelPriceFilter.addEventListener('change', loadHotels);
  }

  if (btnResetHotelFilters) {
    btnResetHotelFilters.addEventListener('click', () => {
      if (hotelLocationFilter) hotelLocationFilter.value = 'ALL';
      if (hotelPriceFilter) {
        hotelPriceFilter.value = 600;
        if (hotelPriceVal) hotelPriceVal.textContent = '$600';
      }
      if (hotelRatingFilter) hotelRatingFilter.value = 'ALL';
      if (hotelAmenityFilter) hotelAmenityFilter.value = 'ALL';
      if (hotelSortFilter) hotelSortFilter.value = 'recommended';
      loadHotels();
    });
  }

  async function loadHotels() {
    if (!hotelsCardsGrid) return;
    hotelsCardsGrid.innerHTML = '<div style="text-align:center;grid-column:1/-1;padding:2rem;color:var(--text-muted);"><div class="spinner" style="margin:0 auto 1rem auto;"></div>Loading curated stays...</div>';

    const location = hotelLocationFilter ? hotelLocationFilter.value : 'ALL';
    const maxPrice = hotelPriceFilter ? hotelPriceFilter.value : '600';
    const minRating = hotelRatingFilter ? hotelRatingFilter.value : 'ALL';
    const amenity = hotelAmenityFilter ? hotelAmenityFilter.value : 'ALL';
    const sortBy = hotelSortFilter ? hotelSortFilter.value : 'recommended';

    const params = new URLSearchParams();
    if (currentHotelCategory && currentHotelCategory !== 'ALL') {
      params.append('category', currentHotelCategory);
    }
    if (location && location !== 'ALL') {
      params.append('location', location);
    }
    if (maxPrice && !isNaN(Number(maxPrice))) {
      params.append('maxPrice', maxPrice);
    }
    if (minRating && minRating !== 'ALL') {
      params.append('minRating', minRating);
    }
    if (amenity && amenity !== 'ALL') {
      params.append('amenity', amenity);
    }
    if (sortBy) {
      params.append('sortBy', sortBy);
    }

    try {
      const url = params.toString() ? `/api/hotels?${params.toString()}` : '/api/hotels';
      const res = await fetch(url);
      if (!res.ok) throw new Error('Could not load hotel recommendations');
      cachedHotels = await res.json();
      renderHotelCards(cachedHotels);
    } catch (err) {
      hotelsCardsGrid.innerHTML = `<div style="text-align:center;grid-column:1/-1;padding:2rem;color:#ef4444;">${err.message}</div>`;
    }
  }

  function renderHotelCards(hotels) {
    if (!hotelsCardsGrid) return;
    hotelsCardsGrid.innerHTML = '';

    if (!hotels || hotels.length === 0) {
      hotelsCardsGrid.innerHTML = `
        <div style="text-align:center;grid-column:1/-1;padding:3rem 1rem;background:#fff;border-radius:12px;border:1px dashed var(--border);">
          <div style="font-size:2rem;margin-bottom:0.5rem;">🏨</div>
          <h4 style="font-size:1.1rem;color:var(--secondary);margin-bottom:0.25rem;">No Stays Match Your Criteria</h4>
          <p style="font-size:0.85rem;color:var(--text-muted);margin:0;">Try widening your nightly price range or resetting filters to browse all verified stays.</p>
        </div>
      `;
      return;
    }

    hotels.forEach(h => {
      const isUrgent = (h.availabilityStatus || '').toLowerCase().includes('left') || (h.availabilityStatus || '').toLowerCase().includes('last');
      const availClass = isUrgent ? 'avail-status-pill urgency' : 'avail-status-pill';
      const amenitiesChips = (h.amenities || []).slice(0, 3).map(a => `<span class="amenity-chip">${a}</span>`).join('');

      const card = document.createElement('article');
      card.className = 'hotel-card';
      card.innerHTML = `
        <div class="hotel-image-wrapper">
          <img src="${h.imageUrl || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80'}" alt="${h.name}" class="hotel-image" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80'">
          <span class="card-badge" style="position: absolute; top: 10px; left: 10px;">${h.badge || 'Verified Stay'}</span>
        </div>
        <div class="hotel-content">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.25rem;">
            <span style="font-size: 0.8rem; color: var(--text-muted);">${h.location || (h.city + ', ' + h.country)}</span>
            <span class="${availClass}">${h.availabilityStatus || 'Verified Availability'}</span>
          </div>
          <h4 style="font-size: 1.05rem; font-weight: 800; color: var(--secondary); margin: 0 0 0.35rem 0; line-height: 1.35;">${h.name}</h4>
          <div style="font-size: 0.82rem; color: #d97706; font-weight: 700; margin-bottom: 0.4rem;">
            ★ ${h.rating || 4.9} <span style="color: var(--text-muted); font-weight: 500;">(${h.reviewCount || 100} reviews)</span>
          </div>
          <div class="hotel-amenities-chips">${amenitiesChips}</div>
          <div class="hotel-footer-row">
            <div>
              <span style="font-size: 0.75rem; color: var(--text-muted); display: block;">Per night</span>
              <strong style="font-size: 1.25rem; color: var(--primary);">${formatPrice(h.pricePerNight)}</strong>
            </div>
            <div class="hotel-actions-row">
              <button type="button" class="btn btn-outline" onclick="window.openHotelDetails('${h.id}')">View Details</button>
              <button type="button" class="btn btn-primary" onclick="window.openHotelReservation('${h.id}')">Select Room</button>
            </div>
          </div>
        </div>
      `;
      hotelsCardsGrid.appendChild(card);
    });
  }

  // --- Hotel Details Modal Flow ---
  window.openHotelDetails = function(hotelId) {
    const hotel = cachedHotels.find(h => h.id === hotelId);
    if (!hotel) return;
    currentSelectedHotel = hotel;

    if (hotelDetailsTitle) hotelDetailsTitle.textContent = hotel.name;
    if (hotelDetailsSubtitle) hotelDetailsSubtitle.textContent = `${hotel.location || (hotel.city + ', ' + hotel.country)} • ${hotel.category || 'Luxury Stay'}`;
    if (hotelDetailsImage) {
      hotelDetailsImage.src = hotel.imageUrl || '';
      hotelDetailsImage.onerror = () => { hotelDetailsImage.src = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80'; };
    }
    if (hotelDetailsCategoryBadge) hotelDetailsCategoryBadge.textContent = hotel.badge || 'Curated Stay';
    if (hotelDetailsAvailabilityBadge) hotelDetailsAvailabilityBadge.textContent = hotel.availabilityStatus || 'Verified Availability';
    if (hotelDetailsRating) hotelDetailsRating.textContent = `★ ${hotel.rating || 4.9} (${hotel.reviewCount || 120} verified traveler reviews)`;
    if (hotelDetailsLocation) hotelDetailsLocation.textContent = hotel.location || (hotel.city + ', ' + hotel.country);
    if (hotelDetailsPrice) hotelDetailsPrice.innerHTML = `${formatPrice(hotel.pricePerNight)} <span style="font-size: 0.85rem; font-weight: 500; color: var(--text-muted);">/ night</span>`;
    if (hotelDetailsDesc) hotelDetailsDesc.textContent = hotel.description || 'Exclusive boutique accommodation with world-class hospitality.';

    if (hotelDetailsAmenities) {
      hotelDetailsAmenities.innerHTML = (hotel.amenities || []).map(a => `<span class="amenity-chip" style="font-size: 0.82rem; padding: 0.3rem 0.65rem;">✓ ${a}</span>`).join('');
    }

    if (hotelDetailsModal) hotelDetailsModal.classList.add('open');
  };

  function closeHotelDetails() {
    if (hotelDetailsModal) hotelDetailsModal.classList.remove('open');
  }

  if (hotelDetailsCloseBtn) hotelDetailsCloseBtn.addEventListener('click', closeHotelDetails);
  if (btnHotelDetailsBack) btnHotelDetailsBack.addEventListener('click', closeHotelDetails);
  if (btnHotelDetailsBook) {
    btnHotelDetailsBook.addEventListener('click', () => {
      closeHotelDetails();
      if (currentSelectedHotel) {
        window.openHotelReservation(currentSelectedHotel.id);
      }
    });
  }

  // --- Hotel Reservation Modal Flow ---
  let selectedRoomRate = 320;
  window.openHotelReservation = async function(hotelId) {
    const hotel = cachedHotels.find(h => h.id === hotelId) || currentSelectedHotel;
    if (!hotel) return;
    currentSelectedHotel = hotel;

    if (hotelModalTitle) hotelModalTitle.textContent = `Reserve Stay: ${hotel.name}`;
    if (hotelModalSubtitle) hotelModalSubtitle.textContent = `${hotel.location || (hotel.city + ', ' + hotel.country)} • Rating ${hotel.rating}/5`;

    if (hotelGuestName && currentUser) {
      hotelGuestName.value = currentUser.name || '';
    }

    // Set check-in to tomorrow if not set
    if (hotelCheckIn && !hotelCheckIn.value) {
      hotelCheckIn.value = tomorrowStr;
      hotelCheckIn.min = todayStr;
    }

    // Fetch tailored rooms from backend
    if (hotelRoomsList) {
      hotelRoomsList.innerHTML = '<div style="text-align:center;padding:1rem;color:var(--text-muted);">Loading available room suites...</div>';
    }

    try {
      const res = await fetch(`/api/rooms/hotels/${hotelId}`);
      if (!res.ok) throw new Error('Could not fetch rooms');
      const rooms = await res.json();
      renderHotelRooms(rooms, hotel.pricePerNight);
    } catch {
      renderDefaultRooms(hotel.pricePerNight);
    }

    updateHotelTotalPrice();
    if (hotelModal) hotelModal.classList.add('open');
  };

  function renderHotelRooms(rooms, basePrice) {
    if (!hotelRoomsList) return;
    hotelRoomsList.innerHTML = '';
    selectedRoomRate = basePrice;

    rooms.forEach((r, idx) => {
      const isFirst = idx === 0;
      if (isFirst) selectedRoomRate = r.pricePerNight || basePrice;

      const roomDiv = document.createElement('label');
      roomDiv.style.border = isFirst ? '2px solid var(--primary)' : '1px solid var(--border)';
      roomDiv.style.borderRadius = '10px';
      roomDiv.style.padding = '0.75rem 1rem';
      roomDiv.style.display = 'flex';
      roomDiv.style.justifyContent = 'space-between';
      roomDiv.style.alignItems = 'center';
      roomDiv.style.cursor = 'pointer';
      roomDiv.style.transition = 'all 0.2s';
      roomDiv.className = 'room-option-item';

      roomDiv.innerHTML = `
        <div style="display: flex; align-items: center; gap: 0.75rem;">
          <input type="radio" name="hotelRoomOption" value="${r.pricePerNight || basePrice}" data-room-name="${r.roomType}" ${isFirst ? 'checked' : ''} style="accent-color: var(--primary);">
          <div>
            <strong style="font-size: 0.92rem; color: var(--secondary);">${r.roomType}</strong>
            <div style="font-size: 0.78rem; color: var(--text-muted);">${r.bedType} &bull; ${r.capacity} &bull; ${(r.amenities || []).slice(0, 2).join(', ')}</div>
          </div>
        </div>
        <div style="text-align: right;">
          <strong style="font-size: 1.05rem; color: var(--primary);">${formatPrice(r.pricePerNight || basePrice)}</strong>
          <span style="font-size: 0.75rem; color: var(--text-muted); display: block;">/ night</span>
        </div>
      `;

      roomDiv.addEventListener('click', () => {
        document.querySelectorAll('.room-option-item').forEach(el => el.style.border = '1px solid var(--border)');
        roomDiv.style.border = '2px solid var(--primary)';
        const radio = roomDiv.querySelector('input[type="radio"]');
        if (radio) {
          radio.checked = true;
          selectedRoomRate = Number(radio.value);
          updateHotelTotalPrice();
        }
      });

      hotelRoomsList.appendChild(roomDiv);
    });
  }

  function renderDefaultRooms(basePrice) {
    if (!hotelRoomsList) return;
    hotelRoomsList.innerHTML = `
      <label class="room-option-item" style="border: 2px solid var(--primary); border-radius: 10px; padding: 0.75rem 1rem; display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
        <div style="display: flex; align-items: center; gap: 0.75rem;">
          <input type="radio" name="hotelRoomOption" value="${basePrice}" data-room-name="Standard Deluxe Suite" checked style="accent-color: var(--primary);">
          <div>
            <strong style="font-size: 0.92rem; color: var(--secondary);">Standard Deluxe Suite</strong>
            <div style="font-size: 0.78rem; color: var(--text-muted);">1 King Bed &bull; 2 Guests &bull; City/Sea View</div>
          </div>
        </div>
        <div style="text-align: right;">
          <strong style="font-size: 1.05rem; color: var(--primary);">${formatPrice(basePrice)}</strong>
          <span style="font-size: 0.75rem; color: var(--text-muted); display: block;">/ night</span>
        </div>
      </label>
    `;
    selectedRoomRate = basePrice;
    updateHotelTotalPrice();
  }

  function updateHotelTotalPrice() {
    const nights = hotelNights ? Number(hotelNights.value) : 3;
    const total = selectedRoomRate * nights;
    if (hotelTotalFare) hotelTotalFare.textContent = formatPrice(total);
  }

  if (hotelNights) hotelNights.addEventListener('change', updateHotelTotalPrice);

  function closeHotelModal() {
    if (hotelModal) hotelModal.classList.remove('open');
  }

  if (hotelModalCloseBtn) hotelModalCloseBtn.addEventListener('click', closeHotelModal);
  if (btnCancelHotelModal) btnCancelHotelModal.addEventListener('click', closeHotelModal);

  // Submit Hotel Booking to Backend
  if (hotelReservationForm) {
    hotelReservationForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!currentSelectedHotel) return;

      const guestName = hotelGuestName ? hotelGuestName.value.trim() : 'Arun Patel';
      const checkInDate = hotelCheckIn ? hotelCheckIn.value : tomorrowStr;
      const nights = hotelNights ? Number(hotelNights.value) : 3;
      const checkedRadio = document.querySelector('input[name="hotelRoomOption"]:checked');
      const roomType = checkedRadio ? (checkedRadio.getAttribute('data-room-name') || 'Deluxe Suite') : 'Deluxe Suite';
      const total = selectedRoomRate * nights;

      if (btnConfirmHotelStay) {
        btnConfirmHotelStay.disabled = true;
        btnConfirmHotelStay.textContent = 'Reserving Room...';
      }

      try {
        const res = await fetch('/api/bookings/hotel', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            hotelId: currentSelectedHotel.id,
            guestName: guestName,
            guestEmail: currentUser ? currentUser.email : 'traveler@example.com',
            checkInDate: checkInDate,
            nights: nights,
            roomType: roomType,
            totalFare: total
          })
        });
        const data = await res.json();
        closeHotelModal();
        showToast(`Stay Confirmed! Booking Ref: ${data.bookingReference || 'HTL-8910'}`);
      } catch {
        closeHotelModal();
        showToast(`Demonstration Stay Confirmed! Ref: HTL-${Math.floor(10000 + Math.random() * 90000)}`);
      } finally {
        if (btnConfirmHotelStay) {
          btnConfirmHotelStay.disabled = false;
          btnConfirmHotelStay.textContent = 'Book Room (Demo)';
        }
      }
    });
  }

  // ============================================================
  // MODULE 9: VIP Travel Deals Hub & Saved Deals
  // ============================================================
  let currentDealCategory = 'ALL';
  let cachedDeals = [];
  let currentSelectedDeal = null;

  const dealCategoryPills = document.getElementById('dealCategoryPills');
  const vipDealsGrid = document.getElementById('vipDealsGrid');
  const btnExploreMembershipTiers = document.getElementById('btnExploreMembershipTiers');

  // Deal Modal Elements
  const vipDealModal = document.getElementById('vipDealModal');
  const vipDealModalTag = document.getElementById('vipDealModalTag');
  const vipDealModalTitle = document.getElementById('vipDealModalTitle');
  const vipDealModalImage = document.getElementById('vipDealModalImage');
  const vipDealModalDiscountBadge = document.getElementById('vipDealModalDiscountBadge');
  const vipDealModalValidity = document.getElementById('vipDealModalValidity');
  const vipDealModalDesc = document.getElementById('vipDealModalDesc');
  const vipDealModalOrigPrice = document.getElementById('vipDealModalOrigPrice');
  const vipDealModalSavings = document.getElementById('vipDealModalSavings');
  const vipDealModalDiscountedPrice = document.getElementById('vipDealModalDiscountedPrice');
  const vipDealModalBenefits = document.getElementById('vipDealModalBenefits');
  const vipDealModalEligibility = document.getElementById('vipDealModalEligibility');
  const btnSaveDealInModal = document.getElementById('btnSaveDealInModal');
  const btnBookVipDealNow = document.getElementById('btnBookVipDealNow');
  const btnVipDealModalClose = document.getElementById('btnVipDealModalClose');
  const vipDealModalCloseBtn = document.getElementById('vipDealModalCloseBtn');

  // Deal category filter pills
  if (dealCategoryPills) {
    dealCategoryPills.querySelectorAll('.category-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        dealCategoryPills.querySelectorAll('.category-pill').forEach(p => {
          p.classList.remove('active');
          p.setAttribute('aria-selected', 'false');
        });
        pill.classList.add('active');
        pill.setAttribute('aria-selected', 'true');
        currentDealCategory = pill.getAttribute('data-dealcat') || 'ALL';
        loadVipDeals(currentDealCategory);
      });
    });
  }

  async function loadVipDeals(category = 'ALL') {
    if (!vipDealsGrid) return;
    vipDealsGrid.innerHTML = '<div style="text-align:center;grid-column:1/-1;padding:2rem;color:var(--text-muted);"><div class="spinner" style="margin:0 auto 1rem auto;"></div>Loading exclusive VIP deals...</div>';

    try {
      const url = category && category !== 'ALL' ? `/api/deals?category=${category}` : '/api/deals';
      const res = await fetch(url);
      if (!res.ok) throw new Error('Could not load VIP travel deals');
      cachedDeals = await res.json();
      renderVipDeals(cachedDeals);
    } catch (err) {
      vipDealsGrid.innerHTML = `<div style="text-align:center;grid-column:1/-1;padding:2rem;color:#ef4444;">${err.message}</div>`;
    }
  }

  function renderVipDeals(deals) {
    if (!vipDealsGrid) return;
    vipDealsGrid.innerHTML = '';

    if (!deals || deals.length === 0) {
      vipDealsGrid.innerHTML = `
        <div style="text-align:center;grid-column:1/-1;padding:3rem 1rem;background:#fff;border-radius:12px;border:1px dashed var(--border);">
          <div style="font-size:2rem;margin-bottom:0.5rem;">👑</div>
          <h4 style="font-size:1.1rem;color:var(--secondary);margin-bottom:0.25rem;">No Deals Currently Active in This Category</h4>
          <p style="font-size:0.85rem;color:var(--text-muted);margin:0;">Select "All VIP Deals" to browse our complete catalog of curated member packages.</p>
        </div>
      `;
      return;
    }

    deals.forEach(deal => {
      const isSaved = userSavedDealIds.has(deal.id);
      const discountPct = deal.discountPercentage || Math.round(((deal.originalPrice - deal.discountedPrice) / deal.originalPrice) * 100);
      const benefitsHtml = (deal.benefits || []).slice(0, 3).map(b => `<li><span class="check">✓</span> ${b}</li>`).join('');

      const card = document.createElement('article');
      card.className = 'vip-deal-card';
      card.innerHTML = `
        <div class="deal-img-wrapper">
          <img src="${deal.imageUrl || 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80'}" alt="${deal.title}" class="deal-img" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80'">
          <span class="deal-discount-badge">SAVE ${discountPct}%</span>
          <button type="button" class="btn-save-deal ${isSaved ? 'saved' : ''}" onclick="window.toggleSaveDeal('${deal.id}', event)" aria-label="${isSaved ? 'Unsave deal' : 'Save deal'}">
            ${isSaved ? '♥' : '♡'}
          </button>
        </div>
        <div class="deal-content">
          <span class="deal-category-tag">${deal.category ? deal.category.replace('_', ' ') : 'VIP PERK'}</span>
          <h4 class="deal-title">${deal.title}</h4>
          <span class="deal-destination">📍 ${deal.destination}</span>
          <ul class="deal-benefits-list">${benefitsHtml}</ul>
          <div class="deal-pricing-row">
            <div>
              <span class="deal-orig-price">${formatPrice(deal.originalPrice)}</span>
              <div class="deal-final-price">${formatPrice(deal.discountedPrice)}</div>
            </div>
            <span style="font-size: 0.75rem; color: #10b981; font-weight: 700; background: #ecfdf5; padding: 0.2rem 0.5rem; border-radius: 4px;">
              Save ${formatPrice(deal.originalPrice - deal.discountedPrice)}
            </span>
          </div>
          <div class="deal-actions-row">
            <button type="button" class="btn btn-outline" onclick="window.openDealDetails('${deal.id}')">View Deal</button>
            <button type="button" class="btn btn-primary" onclick="window.bookVipDeal('${deal.id}')">Book Now</button>
          </div>
        </div>
      `;
      vipDealsGrid.appendChild(card);
    });
  }

  // --- Save Deal Handler ---
  window.toggleSaveDeal = async function(dealId, event) {
    if (event) event.stopPropagation();

    if (!currentUser || !currentUser.email) {
      showToast('Please sign in to save deals to your profile.');
      if (authModal) authModal.classList.add('open');
      return;
    }

    try {
      const res = await fetch('/api/auth/deals/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: currentUser.email, dealId: dealId })
      });
      const data = await res.json();
      if (data.saved) {
        userSavedDealIds.add(dealId);
        showToast('Deal saved to your VoyageX profile! ♥');
      } else {
        userSavedDealIds.delete(dealId);
        showToast('Deal removed from saved items.');
      }
      currentUser.savedDealIds = Array.from(userSavedDealIds);
      localStorage.setItem('voyagex_active_user', JSON.stringify(currentUser));
      renderVipDeals(cachedDeals);
    } catch {
      // Local fallback
      if (userSavedDealIds.has(dealId)) {
        userSavedDealIds.delete(dealId);
        showToast('Deal removed (demo mode).');
      } else {
        userSavedDealIds.add(dealId);
        showToast('Deal saved (demo mode)! ♥');
      }
      renderVipDeals(cachedDeals);
    }
  };

  // --- View Deal Details Modal ---
  window.openDealDetails = function(dealId) {
    const deal = cachedDeals.find(d => d.id === dealId);
    if (!deal) return;
    currentSelectedDeal = deal;

    const discountPct = deal.discountPercentage || Math.round(((deal.originalPrice - deal.discountedPrice) / deal.originalPrice) * 100);
    const savingsAmount = deal.originalPrice - deal.discountedPrice;

    if (vipDealModalTitle) vipDealModalTitle.textContent = deal.title;
    if (vipDealModalTag) vipDealModalTag.textContent = (deal.category || 'VIP ESCAPE').replace('_', ' ');
    if (vipDealModalImage) {
      vipDealModalImage.src = deal.imageUrl || '';
      vipDealModalImage.onerror = () => { vipDealModalImage.src = 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80'; };
    }
    if (vipDealModalDiscountBadge) vipDealModalDiscountBadge.textContent = `SAVE ${discountPct}%`;
    if (vipDealModalValidity) vipDealModalValidity.textContent = deal.validity || 'Valid until Nov 30, 2026';
    if (vipDealModalDesc) vipDealModalDesc.textContent = deal.description || 'Exclusive packaged vacation package.';

    if (vipDealModalOrigPrice) vipDealModalOrigPrice.textContent = formatPrice(deal.originalPrice);
    if (vipDealModalSavings) vipDealModalSavings.textContent = `-${formatPrice(savingsAmount)} (${discountPct}% OFF)`;
    if (vipDealModalDiscountedPrice) vipDealModalDiscountedPrice.textContent = formatPrice(deal.discountedPrice);

    if (vipDealModalBenefits) {
      vipDealModalBenefits.innerHTML = (deal.benefits || []).map(b => `<li><strong style="color:var(--primary);">✓</strong> ${b}</li>`).join('');
    }
    if (vipDealModalEligibility) {
      vipDealModalEligibility.textContent = deal.eligibility || 'Open to all VoyageX members. Free alterations with 72h advance notice.';
    }

    if (btnSaveDealInModal) {
      const isSaved = userSavedDealIds.has(deal.id);
      btnSaveDealInModal.innerHTML = isSaved ? '<span>♥ Saved in Profile</span>' : '<span>♡ Save Deal</span>';
      btnSaveDealInModal.onclick = () => {
        window.toggleSaveDeal(deal.id);
        const nowSaved = userSavedDealIds.has(deal.id);
        btnSaveDealInModal.innerHTML = nowSaved ? '<span>♥ Saved in Profile</span>' : '<span>♡ Save Deal</span>';
      };
    }

    if (btnBookVipDealNow) {
      btnBookVipDealNow.onclick = () => {
        closeDealDetails();
        window.bookVipDeal(deal.id);
      };
    }

    if (vipDealModal) vipDealModal.classList.add('open');
  };

  function closeDealDetails() {
    if (vipDealModal) vipDealModal.classList.remove('open');
  }

  if (vipDealModalCloseBtn) vipDealModalCloseBtn.addEventListener('click', closeDealDetails);
  if (btnVipDealModalClose) btnVipDealModalClose.addEventListener('click', closeDealDetails);

  // --- Book VIP Deal Workflow ---
  window.bookVipDeal = async function(dealId) {
    const deal = cachedDeals.find(d => d.id === dealId) || currentSelectedDeal;
    if (!deal) return;

    // Apply promo voucher automatically
    window.applyPromoCode('VOYAGE30');
    showToast(`VIP Deal "${deal.title}" selected! 30% member discount unlocked.`);

    // If destination matches one of our top destinations, prefill origin/destination
    if (destInput && deal.destination) {
      const destParts = deal.destination.split(',');
      const cityName = destParts[0].trim();
      destInput.value = cityName;
    }

    // Scroll to search or open booking
    const searchSection = document.getElementById('searchSection');
    if (searchSection) {
      searchSection.scrollIntoView({ behavior: 'smooth' });
    }
  };

  // ============================================================
  // MODULE 10: VIP Membership Matrix & Tier Upgrade
  // ============================================================
  const membershipModal = document.getElementById('membershipModal');
  const membershipModalCloseBtn = document.getElementById('membershipModalCloseBtn');

  function openMembershipModal() {
    if (membershipModal) membershipModal.classList.add('open');
    updateMembershipModalTiers();
  }

  function closeMembershipModal() {
    if (membershipModal) membershipModal.classList.remove('open');
  }

  if (btnExploreMembershipTiers) btnExploreMembershipTiers.addEventListener('click', openMembershipModal);
  if (menuMembership) {
    menuMembership.addEventListener('click', () => {
      if (userMenuDropdown) userMenuDropdown.classList.remove('show');
      openMembershipModal();
    });
  }
  if (membershipModalCloseBtn) membershipModalCloseBtn.addEventListener('click', closeMembershipModal);

  function updateMembershipModalTiers() {
    const currentTier = currentUser ? (currentUser.tier || 'VoyageX Explorer') : 'VoyageX Explorer';
    const btnExplorer = document.getElementById('btnSelectExplorerTier');
    const btnPlus = document.getElementById('btnSelectPlusTier');
    const btnElite = document.getElementById('btnSelectEliteTier');

    if (btnExplorer) {
      btnExplorer.textContent = currentTier.includes('Explorer') ? 'Current Plan ✓' : 'Select Explorer';
      btnExplorer.disabled = currentTier.includes('Explorer');
    }
    if (btnPlus) {
      btnPlus.textContent = currentTier.includes('Plus') ? 'Current Plan ✓' : 'Upgrade to Plus (Demo)';
      btnPlus.disabled = currentTier.includes('Plus');
    }
    if (btnElite) {
      btnElite.textContent = currentTier.includes('Elite') ? 'Current Plan ✓' : 'Upgrade to Elite (Demo)';
      btnElite.disabled = currentTier.includes('Elite');
    }
  }

  window.selectMembershipTier = async function(tierName) {
    if (!currentUser || !currentUser.email) {
      showToast('Please sign in or create an account to activate membership.');
      closeMembershipModal();
      if (authModal) authModal.classList.add('open');
      return;
    }

    try {
      const res = await fetch('/api/auth/membership/upgrade', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: currentUser.email, tier: tierName })
      });
      const data = await res.json();
      if (res.ok) {
        currentUser.tier = data.tier || tierName;
        updateNavUserProfile(currentUser);
        closeMembershipModal();
        showToast(`Congratulations! You are now a ${currentUser.tier} member! 👑`);
      }
    } catch {
      currentUser.tier = tierName;
      updateNavUserProfile(currentUser);
      closeMembershipModal();
      showToast(`Simulated upgrade to ${tierName} complete!`);
    }
  };

  // ============================================================
  // MODULE 11: Saved Deals Drawer Modal
  // ============================================================
  const savedDealsModal = document.getElementById('savedDealsModal');
  const savedDealsModalCloseBtn = document.getElementById('savedDealsModalCloseBtn');
  const savedDealsList = document.getElementById('savedDealsList');

  if (menuSavedDeals) {
    menuSavedDeals.addEventListener('click', async () => {
      if (userMenuDropdown) userMenuDropdown.classList.remove('show');
      if (savedDealsModal) savedDealsModal.classList.add('open');
      await loadSavedDeals();
    });
  }

  if (savedDealsModalCloseBtn) {
    savedDealsModalCloseBtn.addEventListener('click', () => {
      if (savedDealsModal) savedDealsModal.classList.remove('open');
    });
  }

  async function loadSavedDeals() {
    if (!savedDealsList) return;
    savedDealsList.innerHTML = '<div style="text-align:center;padding:2rem;color:var(--text-muted);">Loading saved deals...</div>';

    if (!currentUser || !currentUser.email) {
      savedDealsList.innerHTML = '<div style="text-align:center;padding:2rem;color:var(--text-muted);">Please sign in to view your saved travel deals.</div>';
      return;
    }

    try {
      const res = await fetch(`/api/auth/deals/saved?email=${encodeURIComponent(currentUser.email)}`);
      if (!res.ok) throw new Error('Could not fetch saved deals');
      const deals = await res.json();
      renderSavedDealsList(deals);
    } catch {
      // Fallback from cachedDeals matching userSavedDealIds
      const matches = cachedDeals.filter(d => userSavedDealIds.has(d.id));
      renderSavedDealsList(matches);
    }
  }

  function renderSavedDealsList(deals) {
    if (!savedDealsList) return;
    savedDealsList.innerHTML = '';

    if (!deals || deals.length === 0) {
      savedDealsList.innerHTML = `
        <div style="text-align:center;padding:2rem;color:var(--text-muted);">
          <div style="font-size:2.5rem;margin-bottom:0.5rem;">♡</div>
          <h4 style="font-size:1.1rem;color:var(--secondary);margin-bottom:0.25rem;">No Saved Deals Yet</h4>
          <p style="font-size:0.85rem;margin:0;">Browse our Exclusive VIP Travel Deals section and click the heart icon on any deal to save it for later.</p>
        </div>
      `;
      return;
    }

    deals.forEach(d => {
      const item = document.createElement('div');
      item.style.border = '1px solid var(--border)';
      item.style.borderRadius = '12px';
      item.style.padding = '0.85rem 1rem';
      item.style.display = 'flex';
      item.style.alignItems = 'center';
      item.style.justifyContent = 'space-between';
      item.style.gap = '1rem';
      item.style.background = '#ffffff';

      item.innerHTML = `
        <div style="display: flex; align-items: center; gap: 0.75rem; flex: 1;">
          <img src="${d.imageUrl}" alt="${d.title}" style="width: 60px; height: 60px; border-radius: 8px; object-fit: cover;">
          <div>
            <h5 style="margin: 0 0 2px 0; font-size: 0.95rem; color: var(--secondary);">${d.title}</h5>
            <span style="font-size: 0.78rem; color: var(--text-muted);">📍 ${d.destination} &bull; Save ${d.discountPercentage}%</span>
            <div style="font-size: 0.95rem; font-weight: 800; color: var(--primary); margin-top: 2px;">${formatPrice(d.discountedPrice)}</div>
          </div>
        </div>
        <div style="display: flex; gap: 0.5rem;">
          <button type="button" class="btn btn-outline" style="padding: 0.35rem 0.65rem; font-size: 0.78rem;" onclick="if(savedDealsModal)savedDealsModal.classList.remove('open');window.openDealDetails('${d.id}');">View</button>
          <button type="button" class="btn btn-primary" style="padding: 0.35rem 0.65rem; font-size: 0.78rem;" onclick="if(savedDealsModal)savedDealsModal.classList.remove('open');window.bookVipDeal('${d.id}');">Book Now</button>
        </div>
      `;
      savedDealsList.appendChild(item);
    });
  }

  // ============================================================
  // MODULE 12: My Trips Modal
  // ============================================================
  const myTripsModal = document.getElementById('myTripsModal');
  const btnMyTrips = document.getElementById('btnMyTrips');
  const myTripsModalCloseBtn = document.getElementById('myTripsModalCloseBtn');

  function openMyTripsModal() {
    const list = document.getElementById('myTripsList');
    if (list) list.innerHTML = '<div style="text-align:center;padding:1.5rem;color:var(--text-muted);">Loading active reservations...</div>';
    if (myTripsModal) myTripsModal.classList.add('open');

    fetch('/api/trips/my-trips')
      .then(res => res.json())
      .then(trips => renderMyTrips(trips))
      .catch(err => {
        if (list) list.innerHTML = `<div style="text-align:center;color:#ef4444;">${err.message}</div>`;
      });
  }

  if (btnMyTrips) btnMyTrips.addEventListener('click', openMyTripsModal);
  if (menuMyTrips) {
    menuMyTrips.addEventListener('click', () => {
      if (userMenuDropdown) userMenuDropdown.classList.remove('show');
      openMyTripsModal();
    });
  }
  if (myTripsModalCloseBtn) {
    myTripsModalCloseBtn.addEventListener('click', () => {
      if (myTripsModal) myTripsModal.classList.remove('open');
    });
  }

  function renderMyTrips(trips) {
    const list = document.getElementById('myTripsList');
    if (!list) return;

    list.innerHTML = '';
    if (!trips || trips.length === 0) {
      list.innerHTML = '<div style="text-align:center;padding:1.5rem;color:var(--text-muted);">No active bookings yet. Book a flight or hotel above!</div>';
      return;
    }

    trips.forEach(t => {
      const item = document.createElement('div');
      item.style.border = '1px solid var(--border)';
      item.style.borderRadius = '12px';
      item.style.padding = '1rem';
      item.style.background = '#ffffff';

      item.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
          <div>
            <span style="font-size: 0.75rem; font-weight: 700; color: #10b981; background: #ecfdf5; padding: 0.2rem 0.5rem; border-radius: 4px;">CONFIRMED</span>
            <h4 style="margin: 0.4rem 0 0.2rem 0; font-size: 1.05rem; color: var(--secondary);">${t.airline || t.title} (${t.flightNumber || 'Res'})</h4>
            <div style="font-size: 0.82rem; color: var(--text-muted);">${t.origin || 'DEL'} &rarr; ${t.destination || 'DXB'} &bull; Date: ${t.departureDate || '2026-11-01'}</div>
          </div>
          <div style="text-align: right;">
            <div style="font-size: 0.8rem; color: var(--text-muted);">Ref: <strong>${t.bookingReference || 'VX-DEMO'}</strong></div>
            <div style="font-size: 1.1rem; font-weight: 800; color: var(--primary);">${formatPrice(t.totalFare || 380)}</div>
          </div>
        </div>

        <div style="display: flex; gap: 0.5rem; justify-content: flex-end; border-top: 1px dashed var(--border); padding-top: 0.75rem; margin-top: 0.75rem;">
          <button type="button" class="btn btn-outline" style="font-size: 0.78rem; padding: 0.35rem 0.75rem;" onclick="window.openSeatMapForFlight('${t.flightNumber || 'AI-101'}');">
            💺 View / Change Seat
          </button>
          <button type="button" class="btn btn-outline" style="font-size: 0.78rem; padding: 0.35rem 0.75rem;" onclick="document.getElementById('flightStatusSection')?.scrollIntoView({behavior:'smooth'});if(myTripsModal)myTripsModal.classList.remove('open');">
            🔔 Live Status
          </button>
          <button type="button" class="btn btn-outline" style="font-size: 0.78rem; padding: 0.35rem 0.75rem; color: #ef4444; border-color: #fca5a5;" onclick="window.openCancellationModal('${t.bookingReference || 'VX-DEMO'}', ${t.totalFare || 380});if(myTripsModal)myTripsModal.classList.remove('open');">
            ❌ Cancel &amp; Refund
          </button>
        </div>
      `;
      list.appendChild(item);
    });
  }

  // --- Initial Operations on Load ---
  loadHotels();
  loadVipDeals('ALL');
  loadLiveFlightStatus('ALL', '');
  loadReviews('ALL', 'NEWEST');
  loadRecommendations('Romantic', 'Beach');
});


