/* Script for profile.html */

var socket = null;

/*
 * Called when the webpage is loaded. Retrieves user's
 * information from login-user-info.json and passes that
 * information to displayInfo.
 */
function initialize()
{
    fetch("../login-user-info.json")
        .then(response => response.json())
        .then(data => {
            displayInfo(data);
            const orderHistBtn = document.getElementById("order-hist");
            orderHistBtn.addEventListener("click", (e) => {
                e.preventDefault();
                socket = new WebSocket("ws://127.0.0.1:8888");
                socket.onopen = () => {
                    socket.send(`GETORDERHISTORY,${data.userID}`);
                }
                socket.onmessage = (e) => {
                    console.log(e.data);
                    if (e.data.toString().toUpperCase() === "SUCCESS") {
                        window.location.href = `order_hist.html?id=${data.userID}`;
                    }
                    socket.close();
                }
            });
        });
}

/*
 * Checks if user has valid displayable data. If so, display.
 * Else, leave as default.
 */
function displayInfo(user_data)
{   
    // Display user's full name
    if (user_data.firstName !== "" && user_data.lastName !== "") {
        const fullName = document.getElementById("fullName");
        fullName.innerHTML = `${user_data.firstName} ${user_data.lastName}`;
    }

    // Display user's email
    if (user_data.email !== "") {
        const email = document.getElementById("Email");
        email.innerHTML = `${user_data.email}`;
    }

    // Display user's last 4 credit card number
    if (user_data.cardnum !== "") {
        const card = document.getElementById("cardnum");
        card.innerHTML = `XXXX-XXXX-XXXX-${user_data.cardnum.slice(-4)}`;
    }

    // Display exp. month
    if (user_data.expmonth !== "") {
        const expmonth = document.getElementById("expmonth");
        const str_expmonth = convertMonthToString(user_data.expmonth)
        expmonth.innerHTML = `${str_expmonth}`;
    }

    // Display exp. date
    if (user_data.expdate !== "") {
        const expdate = document.getElementById("expdate");
        expdate.innerHTML = `20${user_data.expdate}`;
    }

    // Display cvv
    if (user_data.securitynum !== "") {
        const cvv = document.getElementById("securitynum");
        cvv.innerHTML = `${user_data.securitynum}`;
    }
    
    // Display shipping info
    const shipping = `${user_data.shippingAddressLine1} ${user_data.shippingAddressLine2} ${user_data.shippingCity} ${user_data.shippingState} ${user_data.shippingZip}`;
    if (shipping !== "") {
        document.getElementById("shipping_addr").innerHTML = shipping;
    }

    // Display billing info
    const billing = `${user_data.billingAddressLine1} ${user_data.billingAddressLine2} ${user_data.billingCity} ${user_data.billingState} ${user_data.billingZip}`;
    if (billing !== shipping) {
        document.getElementById("billing_addr").innerHTML = billing;
    }
}

/*
 * Converts the integer representation of the month
 * to its string value.
 */
function convertMonthToString(month)
{
    switch (month)
    {
        case "01":
            month = "January";
            break;
        case "02":
            month = "February";
            break;
        case "03":
            month = "March";
            break;
        case "04":
            month = "April"
            break;
        case "05":
            month = "May";
            break;
        case "06":
            month = "June";
            break;
        case "07":
            month = "July";
            break;
        case "08":
            month = "August";
            break;
        case "09":
            month = "September";
            break;
        case "10":
            month = "October";
            break;
        case "11":
            month = "November";
            break;
        case "12":
            month = "December";
            break;
    }
    return month;
}

window.onload = initialize;