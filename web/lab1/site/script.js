document.addEventListener("DOMContentLoaded", function () {
        const form = document.getElementById("point-form");
        const yInput = document.getElementById("y-value");
        const tableBody = document.getElementById("results-table-body");
        const clockElement = document.getElementById('clock');

        const errorElements = {
            x: document.getElementById("x-error"),
            y: document.getElementById("y-error"),
            r: document.getElementById("r-error"),
            response: document.getElementById("response-error"),
        };

        const storageRadios = document.querySelectorAll('input[name="storage-method"]');
        let currentStorageMethod = 'localStorage';

        const storageManager = {
                    localStorage: {
                        get: () => JSON.parse(localStorage.getItem('resultsTable') || '[]'),
                        set: (data) => localStorage.setItem('resultsTable', JSON.stringify(data)),
                    },
                    cookie: {
                        get: () => {
                            const cookieValue = document.cookie.split('; ').find(row => row.startsWith('resultsTable='));
                            if (cookieValue) {
                                try {
                                    return JSON.parse(decodeURIComponent(cookieValue.split('=')[1]));
                                } catch (e) { return []; }
                            }
                            return [];
                        },
                        set: (data) => {
                            const jsonString = encodeURIComponent(JSON.stringify(data));
                            document.cookie = `resultsTable=${jsonString}; path=/; max-age=31536000; SameSite=Lax`;
                        },
                    },
                };


        function updateClock() {
            clockElement.textContent = new Date().toLocaleTimeString();
        }
        updateClock();
        setInterval(updateClock, 1000);
        renderTable();

        form.addEventListener("submit", (event) => {
            event.preventDefault();
            clearErrors();
            const formData = validateAndGetData();
            if (formData) {
                sendRequest(formData);
            }
        });

        function clearErrors() {
            Object.values(errorElements).forEach(el => el.textContent = "");
        }

        function validateAndGetData() {
            let isFormCorrect = true;
            let y_num;

            const x_element = document.querySelector('input[name="x-value"]:checked');
            if (!x_element) {
                errorElements.x.textContent = "Необходимо выбрать значение X.";
                isFormCorrect = false;
            }

            const r_element = document.querySelector('input[name="r-values"]:checked');
            if (!r_element) {
                errorElements.r.textContent = "Необходимо выбрать радиус R.";
                isFormCorrect = false;
            }

            const yValue = yInput.value.trim().replace(",", ".");
            if (yValue === "") {
                errorElements.y.textContent = "Поле Y не может быть пустым.";
                isFormCorrect = false;
            } else if (!/^[-]?\d+([.,]\d+)?$/.test(yValue) || yValue.endsWith('.')) {
                errorElements.y.textContent = "Y должен быть корректным числом.";
                isFormCorrect = false;
            } else {
                y_num = parseFloat(yValue);
                if (y_num <= -5 || y_num >= 5) {
                    errorElements.y.textContent = "Y должен быть в диапазоне (-5 ... 5).";
                    isFormCorrect = false;
                } else if (yValue.length > 8) {
                    errorElements.y.textContent = "Y должен содержать менее 8 знаков.";
                    isFormCorrect = false;
                }
            }

            if (isFormCorrect) {
                return {
                    x: x_element.value,
                    y: y_num,
                    r: r_element.value
                };
            }

            return null;
        }
        function sendRequest(data) {
        const url = `/fcgi-bin/server.jar?x=${encodeURIComponent(data.x)}&y=${encodeURIComponent(data.y)}&r=${encodeURIComponent(data.r)}`;
              console.log("Отправка запроса на URL:", url);
              fetch(url)
                .then((response) => {
                  if (!response.ok) {
                    console.error(response.statusText);
                  }
                  return response.text();
                })
                .then((data) => {
                  console.log(data);
                  const JSONdata = JSON.parse(data);
                  updateTableWithNewResult(JSONdata);
                })
                .catch((error) => {
                  console.error(error);
                  responseError.innerText = error;
                });
}
         function updateTableWithNewResult(newResultData) {
            let allData = storageManager[currentStorageMethod].get();
            allData.unshift(newResultData);
            storageManager[currentStorageMethod].set(allData);
            renderTable();
        }
        function renderTable() {
                    tableBody.innerHTML = '';
                    const data = storageManager[currentStorageMethod].get();
                    data.forEach(rowData => {
                        const newRow = document.createElement("tr");
                        newRow.innerHTML = `
                            <td>${rowData.x}</td>
                            <td>${rowData.y}</td>
                            <td>${rowData.r}</td>
                            <td>${rowData.isHit ? "Да" : "Нет"}</td>
                            <td>${rowData.curTime}</td>
                            <td>${rowData.execTime.toFixed(6)} с</td>
                        `;
                        tableBody.appendChild(newRow);
                    });
                }
                function updateTable(newResultData) {
                    let allData = storageManager[currentStorageMethod].get();
                    allData.unshift(newResultData);
                    storageManager[currentStorageMethod].set(allData);
                    renderTable();
                }

        const colorPicker = document.getElementById('color-picker');
        const schemeButtons = document.querySelectorAll('.color-controls button');
    
        function hexToRgb(hex) {
            let r = 0, g = 0, b = 0;
            if (hex.length == 4) {
                r = parseInt(hex[1] + hex[1], 16);
                g = parseInt(hex[2] + hex[2], 16);
                b = parseInt(hex[3] + hex[3], 16);
            } else if (hex.length == 7) {
                r = parseInt(hex[1] + hex[2], 16);
                g = parseInt(hex[3] + hex[4], 16);
                b = parseInt(hex[5] + hex[6], 16);
            }
            return { r, g, b };
        }
    
        function rgbToHsl({ r, g, b }) {
            r /= 255; g /= 255; b /= 255;
            let max = Math.max(r, g, b), min = Math.min(r, g, b);
            let h = 0, s, l = (max + min) / 2;
            if (max == min) {
                h = s = 0;
            } else {
                let d = max - min;
                s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
                switch (max) {
                    case r: h = (g - b) / d + (g < b ? 6 : 0); break;
                    case g: h = (b - r) / d + 2; break;
                    case b: h = (r - g) / d + 4; break;
                }
                h /= 6;
            }
            return { h: h * 360, s: s, l: l };
        }
    
        function hslToRgb({ h, s, l }) {
            let r, g, b;
            h /= 360;
            if (s == 0) {
                r = g = b = l;
            } else {
                const hue2rgb = (p, q, t) => {
                    if (t < 0) t += 1;
                    if (t > 1) t -= 1;
                    if (t < 1 / 6) return p + (q - p) * 6 * t;
                    if (t < 1 / 2) return q;
                    if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
                    return p;
                };
                let q = l < 0.5 ? l * (1 + s) : l + s - l * s;
                let p = 2 * l - q;
                r = hue2rgb(p, q, h + 1 / 3);
                g = hue2rgb(p, q, h);
                b = hue2rgb(p, q, h - 1 / 3);
            }
            return { r: Math.round(r * 255), g: Math.round(g * 255), b: Math.round(b * 255) };
        }
        
        function componentToHex(c) {
            const hex = c.toString(16);
            return hex.length == 1 ? "0" + hex : hex;
        }
    
        function rgbToHex({r, g, b}) {
            return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
        }
        
        function isColorDark(hex) {
            const { r, g, b } = hexToRgb(hex);
            return (r * 0.299 + g * 0.587 + b * 0.114) < 140;
        }
    
        function updateColorScheme(baseHex, scheme) {
            const baseRgb = hexToRgb(baseHex);
            const baseHsl = rgbToHsl(baseRgb);
            
            let primaryColor = baseHex;
            let accentColor;
    
            switch (scheme) {
                case 'mono':
                    let monoL = baseHsl.l > 0.5 ? baseHsl.l - 0.2 : baseHsl.l + 0.2;
                    accentColor = rgbToHex(hslToRgb({ h: baseHsl.h, s: baseHsl.s, l: monoL }));
                    break;
                case 'comp':
                    let compH = (baseHsl.h + 180) % 360;
                    accentColor = rgbToHex(hslToRgb({ h: compH, s: baseHsl.s, l: baseHsl.l }));
                    break;
                case 'analog':
                    let analogH = (baseHsl.h + 30) % 360;
                    accentColor = rgbToHex(hslToRgb({ h: analogH, s: baseHsl.s, l: baseHsl.l }));
                    break;
                default:
                    let defaultH = (baseHsl.h + 180) % 360;
                    accentColor = rgbToHex(hslToRgb({ h: defaultH, s: baseHsl.s, l: baseHsl.l }));
            }
    
            document.documentElement.style.setProperty('--primary-color', primaryColor);
            document.documentElement.style.setProperty('--accent-color', accentColor);
            document.documentElement.style.setProperty('--bg-color', accentColor);

            document.documentElement.style.setProperty(
                '--accent-text-color',
                isColorDark(accentColor) ? '#FFFFFF' : '#000000'
            );
        }
    
        schemeButtons.forEach(button => {
            button.addEventListener('click', () => {
                const scheme = button.dataset.scheme;
                updateColorScheme(colorPicker.value, scheme);
            });
        });
    
        colorPicker.addEventListener('input', () => {
            updateColorScheme(colorPicker.value, 'comp');
        });

        updateColorScheme(colorPicker.value, 'mono');

    });

    window.onlyOne = function(checkbox) {
        var checkboxes = document.getElementsByName(checkbox.name);
        checkboxes.forEach((item) => {
            if (item !== checkbox) item.checked = false;
        });
    }


const canvas = document.getElementById("myCanvas");
const ctx = canvas.getContext("2d");

const offsetX = canvas.width / 2;
const offsetY = canvas.height / 2;

const R = 180;

const axisColor = "#333";
const shapeColor = "rgba(70, 130, 180, 0.8)";
const labelColor = "#000";
const font = "14px Arial";

function translateX(x) {
    return offsetX + x;
}

function translateY(y) {
    return offsetY - y;
}

ctx.beginPath();
ctx.strokeStyle = axisColor;
ctx.lineWidth = 2;
ctx.moveTo(0, offsetY);
ctx.lineTo(canvas.width, offsetY);
ctx.moveTo(canvas.width - 10, offsetY - 5);
ctx.lineTo(canvas.width, offsetY);
ctx.lineTo(canvas.width - 10, offsetY + 5);
ctx.moveTo(offsetX, canvas.height);
ctx.lineTo(offsetX, 0);
ctx.moveTo(offsetX - 5, 10);
ctx.lineTo(offsetX, 0);
ctx.lineTo(offsetX + 5, 10);
ctx.stroke();

ctx.fillStyle = labelColor;
ctx.font = font;
ctx.textAlign = "center";
ctx.textBaseline = "middle";

ctx.fillText("R", translateX(R), translateY(-20));
ctx.fillText("R/2", translateX(R / 2), translateY(-20));
ctx.fillText("-R/2", translateX(-R / 2), translateY(-20));
ctx.fillText("-R", translateX(-R), translateY(-20));
ctx.fillText("x", canvas.width - 20, offsetY + 20);

ctx.fillText("R", translateX(20), translateY(R));
ctx.fillText("R/2", translateX(20), translateY(R / 2));
ctx.fillText("-R/2", translateX(25), translateY(-R / 2));
ctx.fillText("-R", translateX(20), translateY(-R));
ctx.fillText("y", offsetX - 20, 20);

function drawTick(x, y, axis) {
    ctx.beginPath();
    ctx.strokeStyle = axisColor;
    ctx.lineWidth = 2;
    if (axis === 'x') {
        ctx.moveTo(translateX(x), offsetY - 5);
        ctx.lineTo(translateX(x), offsetY + 5);
    } else {
        ctx.moveTo(offsetX - 5, translateY(y));
        ctx.lineTo(offsetX + 5, translateY(y));
    }
    ctx.stroke();
}

drawTick(R, 0, 'x');
drawTick(R / 2, 0, 'x');
drawTick(-R / 2, 0, 'x');
drawTick(-R, 0, 'x');
drawTick(0, R, 'y');
drawTick(0, R / 2, 'y');
drawTick(0, -R / 2, 'y');
drawTick(0, -R, 'y');

ctx.beginPath();
ctx.fillStyle = shapeColor;
ctx.strokeStyle = "rgba(0, 0, 139, 1)";
ctx.lineWidth = 2;

ctx.moveTo(translateX(0), translateY(0));
ctx.arc(translateX(0), translateY(0), R / 2, Math.PI, 1.5 * Math.PI, false);
ctx.lineTo(translateX(0), translateY(R/2));
ctx.lineTo(translateX(R), translateY(0));
ctx.lineTo(translateX(R), translateY(-R));
ctx.lineTo(translateX(0), translateY(-R));

ctx.closePath();

ctx.fill();
ctx.stroke();
