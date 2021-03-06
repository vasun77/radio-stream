set scriptPath to POSIX path of ((path to me as text) & "::")
tell application "iTerm"
	tell first window
		set newTab to (create tab with default profile)
		tell newTab
			set row1_col1 to (item 1 of sessions)

			-- creating columns
			tell row1_col1
				set row1_col2 to (split vertically with default profile)
			end tell
			
			-- running commands
			tell row1_col1 to write text "npm run server-desktop-dev"
			tell row1_col1 to set name to "MusicStream - Server"
			tell row1_col2 to write text "cd " & scriptPath & " && npm run start-desktop-dev"
			tell row1_col2 to set name to "MusicStream - Client"
			
		end tell
	end tell
end tell