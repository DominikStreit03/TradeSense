// Minimal React (Dark Mode) - uses React 17 UMD
const { useState, useEffect } = React;

function App() {
    const [trades, setTrades] = useState([]);
    const [stats, setStats] = useState({});
    const [file, setFile] = useState(null);
    const [message, setMessage] = useState("");

    useEffect(() => {
        loadTrades();
        loadStats();
    }, []);

    function loadTrades() {
        fetch("/api/trades")
            .then(r => r.json())
            .then(data => setTrades(data))
            .catch(err => console.error(err));
    }

    function loadStats() {
        fetch("/api/trades/stats")
            .then(r => r.json())
            .then(data => setStats(data))
            .catch(err => console.error(err));
    }

    function handleFileChange(e) {
        setFile(e.target.files[0]);
    }

    function handleUpload(e) {
        e.preventDefault();
        if (!file) {
            setMessage("Bitte eine CSV- oder XLSX-Datei auswählen.");
            return;
        }
        const formData = new FormData();
        formData.append("file", file);
        fetch("/api/trades/upload", { method: "POST", body: formData })
            .then(res => res.json())
            .then(data => {
                if (data.status === "ok") {
                    setMessage("Import successfully.");
                    loadTrades();
                    loadStats();
                } else {
                    setMessage("Fehler: " + (data.message || "Unbekannter Fehler"));
                }
            })
            .catch(err => {
                console.error(err);
                setMessage("Upload fehlgeschlagen.");
            });
    }

    // Farbschema Dark Mode
    const bgColor = "#1e1e1e";          // Seiten-Hintergrund
    const cardBg = "#2a2a2a";           // Cards / Boxen
    const textColor = "#f0f0f0";        // Haupttext
    const tableHeaderBg = "#333";       // Tabellenkopf
    const tableRowBg = "#2a2a2a";       // Tabellenzeilen
    const tableRowAltBg = "#252525";    // Alternative Zeilen

    return (
        React.useEffect(() => {
            document.body.style.backgroundColor = "#121212"; // Hintergrund dunkel
            document.body.style.color = "#e0e0e0";           // Schrift hell
            document.body.style.margin = "0";                // Browser-Rand weg
            document.body.style.padding = "0";
        }, []);
        React.createElement("div", { style: { padding: 16, fontFamily: "Arial", backgroundColor: "#1e1e1e", color: "#e0e0e0" } },
            React.createElement("h1", null, "TradeSense Dashboard"),
            React.createElement("form", { onSubmit: handleUpload, style: { marginBottom: 16 } },
                React.createElement("label", null, "Datei (CSV/XLSX): ",
                    React.createElement("input", { type: "file", accept: ".csv,.xlsx", onChange: handleFileChange })
                ),
                React.createElement("button", { type: "submit", style: { marginLeft: 8 } }, "Upload")
            ),
            React.createElement("div", { style: { marginTop: 8, color: "#4caf50" } }, message),
            React.createElement("div", { style: { display: "flex", marginTop: 16, gap: 16, flexWrap: "wrap" } },
                React.createElement("div", { style: { padding: 12, borderRadius: 8, backgroundColor: cardBg, minWidth: 120 } },
                    React.createElement("h4", null, "Total Trades"),
                    React.createElement("div", null, stats.totalTrades ?? 0)
                ),
                React.createElement("div", { style: { padding: 12, borderRadius: 8, backgroundColor: cardBg, minWidth: 120 } },
                    React.createElement("h4", null, "Sum Profit/Loss"),
                    React.createElement("div", null, (stats.sumProfitLoss ?? 0).toFixed(2))
                ),
                React.createElement("div", { style: { padding: 12, borderRadius: 8, backgroundColor: cardBg, minWidth: 120 } },
                    React.createElement("h4", null, "Avg Profit/Loss"),
                    React.createElement("div", null, (stats.avgProfitLoss ?? 0).toFixed(2))
                ),
                React.createElement("div", { style: { padding: 12, borderRadius: 8, backgroundColor: cardBg, minWidth: 120 } },
                    React.createElement("h4", null, "Win Rate"),
                    React.createElement("div", null, ((stats.winRatePercent ?? 0).toFixed(1)) + " %")
                )
            ),
            React.createElement("div", { style: { marginTop: 24, overflowX: "auto" } },
                React.createElement("table", {
                    style: {
                        borderCollapse: "collapse",
                        width: "100%",
                        color: textColor
                    }
                },
                    React.createElement("thead", { style: { backgroundColor: tableHeaderBg } },
                        React.createElement("tr", null,
                            ["ID","Symbol","Entry","Exit","Qty","P/L","Timestamp","Tags","Notes"].map(h =>
                                React.createElement("th", { key: h, style: { padding: 8, border: "1px solid #555" } }, h)
                            )
                        )
                    ),
                    React.createElement("tbody", null,
                        trades && trades.length > 0 ? trades.map((t, idx) =>
                            React.createElement("tr", {
                                key: t.id,
                                style: { backgroundColor: idx % 2 === 0 ? tableRowBg : tableRowAltBg }
                            },
                                React.createElement("td", { style: { padding: 6, border: "1px solid #555" } }, t.id),
                                React.createElement("td", { style: { padding: 6, border: "1px solid #555" } }, t.symbol),
                                React.createElement("td", { style: { padding: 6, border: "1px solid #555" } }, t.entryPrice),
                                React.createElement("td", { style: { padding: 6, border: "1px solid #555" } }, t.exitPrice),
                                React.createElement("td", { style: { padding: 6, border: "1px solid #555" } }, t.quantity),
                                React.createElement("td", { style: { padding: 6, border: "1px solid #555" } }, t.profitLoss),
                                React.createElement("td", { style: { padding: 6, border: "1px solid #555" } }, t.timestamp),
                                React.createElement("td", { style: { padding: 6, border: "1px solid #555" } }, t.tags ? t.tags.join(", ") : ""),
                                React.createElement("td", { style: { padding: 6, border: "1px solid #555" } }, t.notes)
                            )
                        ) : React.createElement("tr", null,
                            React.createElement("td", { colSpan: 9, style: { padding: 8, textAlign: "center" } }, "Keine Trades verfügbar")
                        )
                    )
                )
            )
        )
    );
}

ReactDOM.render(React.createElement(App, null), document.getElementById("root"));
