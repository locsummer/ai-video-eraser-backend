import SwiftUI

struct AuthView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var isRegister = false
    @State private var email = ""
    @State private var password = ""
    @State private var fullName = ""

    var body: some View {
        ZStack {
            AppTheme.darkBackground.ignoresSafeArea()

            VStack(spacing: 20) {
                HStack {
                    Spacer()
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 26))
                            .foregroundColor(AppTheme.textSecondary)
                    }
                }

                Spacer()

                Text(isRegister ? "Create Account" : "Welcome Back")
                    .font(.system(size: 26, weight: .bold))
                    .foregroundColor(.white)

                Text(isRegister ? "Sign up to receive 50 free credits" : "Log in to sync your videos & membership")
                    .font(.system(size: 13))
                    .foregroundColor(AppTheme.textSecondary)

                VStack(spacing: 14) {
                    if isRegister {
                        CustomTextField(placeholder: "Full Name", text: $fullName)
                    }
                    CustomTextField(placeholder: "Email Address", text: $email)
                    CustomSecureField(placeholder: "Password", text: $password)
                }
                .padding(.top, 10)

                if let error = authViewModel.errorMessage {
                    Text(error)
                        .font(.system(size: 12))
                        .foregroundColor(AppTheme.errorRed)
                }

                Button {
                    Task {
                        if isRegister {
                            await authViewModel.register(email: email, pass: password, name: fullName)
                        } else {
                            await authViewModel.login(email: email, pass: password)
                        }
                        if authViewModel.isAuthenticated {
                            dismiss()
                        }
                    }
                } label: {
                    HStack {
                        if authViewModel.isLoading {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Text(isRegister ? "Register" : "Login")
                                .fontWeight(.bold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(AppTheme.accentPurple)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .disabled(authViewModel.isLoading)

                Button {
                    isRegister.toggle()
                } label: {
                    Text(isRegister ? "Already have an account? Log In" : "Don't have an account? Sign Up")
                        .font(.system(size: 13))
                        .foregroundColor(AppTheme.accentCyan)
                }

                Spacer()
            }
            .padding(24)
        }
    }
}

struct CustomTextField: View {
    let placeholder: String
    @Binding var text: String

    var body: some View {
        TextField(placeholder, text: $text)
            .padding()
            .background(AppTheme.surfaceDark)
            .cornerRadius(12)
            .foregroundColor(.white)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(AppTheme.surfaceLight, lineWidth: 1)
            )
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
    }
}

struct CustomSecureField: View {
    let placeholder: String
    @Binding var text: String

    var body: some View {
        SecureField(placeholder, text: $text)
            .padding()
            .background(AppTheme.surfaceDark)
            .cornerRadius(12)
            .foregroundColor(.white)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(AppTheme.surfaceLight, lineWidth: 1)
            )
    }
}
