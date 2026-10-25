import SwiftUI
import PhotosUI

struct HomeView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var pickedVideoURL: URL?

    @State private var showEditor = false
    @State private var showSubscription = false
    @State private var showAuth = false

    var body: some View {
        NavigationStack {
            ZStack {
                AppTheme.darkBackground.ignoresSafeArea()

                ScrollView {
                    VStack(alignment: .leading, spacing: 22) {
                        // Header
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("AI Video Eraser")
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundColor(.white)
                                Text("Remove unwanted objects & watermark")
                                    .font(.system(size: 13))
                                    .foregroundColor(AppTheme.textSecondary)
                            }

                            Spacer()

                            // Credits Pill
                            Button {
                                if authViewModel.isAuthenticated {
                                    showSubscription = true
                                } else {
                                    showAuth = true
                                }
                            } label: {
                                HStack(spacing: 6) {
                                    Image(systemName: "sparkles")
                                        .foregroundColor(Color.yellow)
                                        .font(.system(size: 13))
                                    Text("\(authViewModel.currentUser?.credits ?? 50) Cr")
                                        .font(.system(size: 13, weight: .semibold))
                                        .foregroundColor(.white)
                                }
                                .padding(.horizontal, 12)
                                .padding(.vertical, 7)
                                .background(AppTheme.surfaceDark)
                                .cornerRadius(20)
                            }
                        }

                        // Hero Picker Card
                        PhotosPicker(selection: $selectedPhotoItem, matching: .videos) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 20)
                                    .fill(
                                        LinearGradient(
                                            colors: [AppTheme.accentPurple, Color(red: 76/255, green: 29/255, blue: 149/255)],
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        )
                                    )
                                    .frame(height: 190)

                                VStack(spacing: 12) {
                                    ZStack {
                                        Circle()
                                            .fill(Color.white.opacity(0.2))
                                            .frame(width: 60, height: 60)
                                        Image(systemName: "plus.rectangle.fill.on.rectangle.fill")
                                            .font(.system(size: 26))
                                            .foregroundColor(.white)
                                    }

                                    Text("Select Video to Erase")
                                        .font(.system(size: 18, weight: .bold))
                                        .foregroundColor(.white)

                                    Text("Supports MP4, MOV up to 500MB")
                                        .font(.system(size: 12))
                                        .foregroundColor(.white.opacity(0.8))
                                }
                            }
                        }
                        .onChange(of: selectedPhotoItem) { newItem in
                            guard let item = newItem else { return }
                            Task {
                                if let movie = try? await item.loadTransferable(type: VideoTransferable.self) {
                                    self.pickedVideoURL = movie.url
                                    self.showEditor = true
                                }
                            }
                        }

                        // Removal Features
                        Text("AI Removal Capabilities")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.white)

                        HStack(spacing: 14) {
                            FeatureBox(
                                title: "Object & Passerby",
                                desc: "Erase moving tourists",
                                icon: "person.crop.circle.badge.xmark"
                            )
                            FeatureBox(
                                title: "Watermark & Text",
                                desc: "Clean logos & timestamps",
                                icon: "character.textbox"
                            )
                        }

                        // VIP Banner
                        Button {
                            showSubscription = true
                        } label: {
                            HStack(spacing: 14) {
                                ZStack {
                                    Circle()
                                        .fill(Color.yellow.opacity(0.15))
                                        .frame(width: 44, height: 44)
                                    Image(systemName: "crown.fill")
                                        .foregroundColor(.yellow)
                                }

                                VStack(alignment: .leading, spacing: 3) {
                                    Text("Upgrade to Pro VIP")
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(.white)
                                    Text("Unlimited duration, 4K export & priority GPU")
                                        .font(.system(size: 12))
                                        .foregroundColor(AppTheme.textSecondary)
                                }

                                Spacer()

                                Image(systemName: "chevron.right")
                                    .foregroundColor(AppTheme.textSecondary)
                            }
                            .padding(16)
                            .background(AppTheme.surfaceDark)
                            .cornerRadius(16)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 10)
                }
            }
            .navigationDestination(isPresented: $showEditor) {
                if let url = pickedVideoURL {
                    EditorView(videoURL: url)
                }
            }
            .sheet(isPresented: $showSubscription) {
                SubscriptionView()
            }
            .sheet(isPresented: $showAuth) {
                AuthView()
            }
            .task {
                await authViewModel.fetchProfile()
            }
        }
    }
}

struct FeatureBox: View {
    let title: String
    let desc: String
    let icon: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 22))
                .foregroundColor(AppTheme.accentCyan)
            Text(title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(.white)
            Text(desc)
                .font(.system(size: 11))
                .foregroundColor(AppTheme.textSecondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(AppTheme.surfaceDark)
        .cornerRadius(14)
    }
}

// Transferable wrapper for loaded video
struct VideoTransferable: Transferable {
    let url: URL

    static var transferRepresentation: some TransferRepresentation {
        FileRepresentation(contentType: .movie) { movie in
            SentTransferredFile(movie.url)
        } importing: { received in
            let tempCopy = FileManager.default.temporaryDirectory.appendingPathComponent(received.file.lastPathComponent)
            try? FileManager.default.removeItem(at: tempCopy)
            try FileManager.default.copyItem(at: received.file, to: tempCopy)
            return Self(url: tempCopy)
        }
    }
}
