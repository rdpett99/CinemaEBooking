/* Script for manage_promos.html */

var socket = null;

function initialize()
{
    socket = new WebSocket("ws://127.0.0.1:8888");
    socket.onopen = () => {
        console.log("Connection to server established.");

        displayPromos();

        const addpromo = document.getElementById("addpromo");
        addpromo.addEventListener("click", (event) => {
            event.preventDefault();
            addPromo();
        });

        const sendPromoBtn = document.getElementById("sendPromoBtn");
        sendPromoBtn.addEventListener("click", (event) => {
            event.preventDefault();
            sendPromo();
        });

        const logoutBtn = document.getElementById("logoutBtn");
        logoutBtn.addEventListener("click", (event) => {
            event.preventDefault();
            logoutAdmin();
        });

        const viewusers = document.getElementById("viewusers");
        viewusers.addEventListener("click", (e) => {
            e.preventDefault();
            getUsers();
        });
    }
}

/*
 * Displays promos in the database.
 */
function displayPromos()
{
    fetch("../promotion-info.json")
        .then(res => res.json())
        .then(data => {
            for (let i = 0; i < data.length; i++) {
                const newdiv = document.createElement("div");
                newdiv.setAttribute("class", "item-col");
                const header = document.createElement("h4");
                header.innerHTML = data[i].promotionCode;
                const description = document.createElement("p");
                description.innerHTML = `${data[i].percentOff}% off for ticket checkout.`;
                newdiv.appendChild(header);
                newdiv.appendChild(description);
                document.getElementById("promos").appendChild(newdiv);
            }
        });
}

/*
 * Sends LOGOUT message to server. Server will clear admin's data
 * from JSON file and redirect to the default homepage.
 */
function logoutAdmin()
{
    socket.send("LOGOUT");

    socket.onmessage = (event) => {
        console.log(event.data);
        if (event.data === "SUCCESS") {

            // Replace the current URL with a new one, and clears admin-home.html from
            // browser history, preventing access to the page. Page should only be
            // accessible by logging in with an admin account., which this solves.
            history.replaceState(null, null, "../homepage.html");
            window.onpopstate = () => {
                history.go(1);
            };
        }
        socket.close();
    }

    socket.onclose = (event) => {
        console.log("WebSocket connection closed with code:", event.code);
    }
}

/*
 * Adds a promo.
 */
function addPromo()
{   
    document.getElementById("addPromoDiv").style.display = 'block';
    const addPromoBtn = document.getElementById("addPromoBtn");
    addPromoBtn.addEventListener("click", (event) => {
        event.preventDefault();
        socket.send(`ADDPROMOTION,${document.getElementById("promocode").value},${document.getElementById("percentoff").value}`);
        
        socket.onmessage = (event) => {
            console.log(event.data);
            if (event.data === "SUCCESS") {
                const newPromo = document.getElementById("newPromo");
                const title = document.createElement("h4");
                title.innerHTML = document.getElementById("promocode").value;
                const description = document.createElement("p");
                description.innerHTML = `Gives a ${document.getElementById("percentoff").value}% discount.`;
                newPromo.appendChild(title);
                newPromo.appendChild(description);
            }
        }
    });
}

/*
 * Sends a promo to subscribed users' emails.
 */
function sendPromo()
{
    socket.send("SENDPROMOTION,take15,15");

    socket.onmessage = (event) => {
        console.log(event.data);
    }
}

/*
 * Gets users before leaving page.
 */
function getUsers()
{
    socket = new WebSocket("ws://127.0.0.1:8888");
    socket.onopen = () => {
        socket.send("GETUSERS")
    }
    socket.onmessage = (e) => {
        console.log(e.data);
        if (e.data === "SUCCESS") {
            window.location.href = "manage_users.html";
        }
        socket.close();
    }
}

window.onload = initialize;