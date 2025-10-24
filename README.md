**Employee Vacation Monitoring System — Plan & Starter Kit**

A single-file plan + starter SQL, API, and front-end snippets to track, approve, and report employee vacations.

1) Goal

Build a reliable system to monitor employee vacation requests, approvals, balances, overlaps, and reporting. Support managers, HR, and employees with notifications and calendar sync.

2) Core features (MVP)

Employee self-service: request vacation (full day, half-day, hourly)

Manager approval flow with comments

Leave balance tracking by leave type (annual, sick, unpaid)

Company holidays and blackout dates

Overlap detection (warn managers when team members overlap)

Calendar export / Google Calendar sync (ICS)

Notifications: email + Slack + in-app

Reports: upcoming vacations, accruals, balance snapshots, usage by team

Audit log and role-based access control (Employee, Manager, HR, Admin)
{
  "email": "john.doe@example4.com",
  "password": "123!"
}
{
    "message": "Login successful",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6ImJhZjgzMmNhLTM5OGUtNGRkNC1hZjFiLTZlZTgzMWExNDY5ZCIsInJvbGUiOiJFTVBMT1lFRSIsInRlYW0iOiJEZXZlbG9wbWVudCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGU0LmNvbSIsImlhdCI6MTc2MTI5MjM0NywiZXhwIjoxNzYxMzc4NzQ3fQ.isO_EQ2OFbDXI7h8umwpjAdo8r8bLojZKjBvLz8M0Gw"
}


from PyQt5.QtWidgets import *
from PyQt5.QtGui import *
from PyQt5.QtCore import Qt
import sys
import requests

API_URL = "http://213.230.64.2:8080/api/auth/login"  # <-- your API endpoint


class LoginUI(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("MTRK Gonorar Tizimi")
        self.resize(800, 500)
        self.setup_ui()

    def setup_ui(self):
        layout = QHBoxLayout(self)

        # -------- Left panel --------
        left = QWidget()
        left.setStyleSheet("background-color: #073B74; color: white;")
        left_layout = QVBoxLayout(left)
        left_layout.addStretch()
        label = QLabel("Shaffoflik. Samaradorlik. Aniq hisobot.")
        label.setStyleSheet("font-size: 18px; font-weight: bold;")
        left_layout.addWidget(label, alignment=Qt.AlignCenter)
        left_layout.addWidget(QLabel("Milliy teleradiokompaniyasining yagona gonorarlar hisobi elektron tizimi."),
                              alignment=Qt.AlignCenter)
        left_layout.addStretch()

        # -------- Right panel --------
        right = QWidget()
        form_layout = QVBoxLayout(right)
        form_layout.addWidget(QLabel("Xush kelibsiz!"))

        self.login = QLineEdit()
        self.login.setPlaceholderText("Email")
        self.password = QLineEdit()
        self.password.setPlaceholderText("Parol")
        self.password.setEchoMode(QLineEdit.Password)

        self.status_label = QLabel("")
        self.status_label.setStyleSheet("color: red;")

        btn = QPushButton("TIZIMGA KIRISH")
        btn.setStyleSheet("background:#0A2E5D; color:white; padding:8px;")
        btn.clicked.connect(self.login_user)

        form_layout.addWidget(self.login)
        form_layout.addWidget(self.password)
        form_layout.addWidget(btn)
        form_layout.addWidget(self.status_label)

        layout.addWidget(left)
        layout.addWidget(right)

    def login_user(self):
        email = self.login.text().strip()
        password = self.password.text().strip()

        if not email or not password:
            self.status_label.setText("Iltimos, login va parolni kiriting.")
            return

        try:
            payload = {"email": email, "password": password}
            response = requests.post(API_URL, json=payload)

            if response.status_code == 200:
                data = response.json()
                if "token" in data:
                    token = data["token"]
                    self.status_label.setStyleSheet("color: green;")
                    self.status_label.setText("Kirish muvaffaqiyatli!")
                    QMessageBox.information(self, "Success", f"Login successful!\nToken:\n{token}")
                    # You can now save the token to a file or open a new window
                else:
                    self.status_label.setText("Login muvaffaqiyatli emas.")
            else:
                try:
                    data = response.json()
                    self.status_label.setText(data.get("message", "Login xatosi."))
                except:
                    self.status_label.setText(f"Server xatosi: {response.status_code}")

        except requests.exceptions.ConnectionError:
            self.status_label.setText("Server bilan ulanishda xatolik.")
        except Exception as e:
            self.status_label.setText(f"Xato: {e}")


if __name__ == "__main__":
    app = QApplication(sys.argv)
    win = LoginUI()
    win.show()
    sys.exit(app.exec_())
